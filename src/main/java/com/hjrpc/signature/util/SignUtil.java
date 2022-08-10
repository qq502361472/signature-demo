package com.hjrpc.signature.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class SignUtil {
    /**
     * 生成带签名的请求链接，只支持get请求
     *
     * @param urlPrefix       url前缀
     * @param accessKeyId     accessKeyId
     * @param accessKeySecret accessKeySecret
     * @param paramMap        paramMap
     * @return url
     */
    public static String generatorSignUrl(String urlPrefix, String accessKeyId, String accessKeySecret, Map<String, Object> paramMap) {
        return urlPrefix + "?" + generatorSignParamsPart(accessKeyId, accessKeySecret, paramMap);
    }

    /**
     * 生成带签名的参数字符串
     *
     * @param accessKeyId     accessKeyId
     * @param accessKeySecret accessKeySecret
     * @param paramMap        paramMap
     * @return String
     */
    public static String generatorSignParamsPart(String accessKeyId, String accessKeySecret, Map<String, Object> paramMap) {
        // 当前时间的时间戳
        long timestamp = System.currentTimeMillis();
        // 补充参数
        paramMap.put("timestamp", timestamp);
        paramMap.put("accessKeyId", accessKeyId);

        String paramsString = paramMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("|"));

        String base64Params = Base64.getEncoder().encodeToString(paramsString.getBytes(StandardCharsets.UTF_8));
        String params;
        try {
            params = URLEncoder.encode(base64Params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("参数进行URL编码失败：", e);
        }

        // 生成签名字符串
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, accessKeySecret.getBytes(StandardCharsets.UTF_8));
        String sign = hMac.digestHex(base64Params);

        // 生成参数串
        return "params=" + params + "&sign=" + sign;
    }

    public static void checkSign(String body, Map<String, String[]> params, Collection<String> paths
            , String accessKeyId, String accessKeySecret, long timestamp, String sign) {
        String allParamsString = getAllParamsString(body, params, paths, accessKeyId, timestamp);

        String newSign = generatorSign(allParamsString, accessKeySecret);
        if (!StrUtil.equals(sign, newSign)) {
            throw new RuntimeException("签名验证失败");
        }
    }

    public static String generatorSign(String allParamsString, String accessKeySecret) {
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, accessKeySecret.getBytes(StandardCharsets.UTF_8));
        return hMac.digestHex(allParamsString);
    }

    private static String getAllParamsString(String body, Map<String, String[]> params, Collection<String> paths, String accessKeyId, long timestamp) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(body)) {
            sb.append(body).append('#');
        }

        if (CollectionUtil.isNotEmpty(params)) {
            params.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(paramEntry -> {
                        String paramValue = Arrays.stream(paramEntry.getValue()).sorted().collect(Collectors.joining(","));
                        sb.append(paramEntry.getKey()).append("=").append(paramValue).append('#');
                    });
        }

        if (CollectionUtil.isNotEmpty(paths)) {
            String pathValues = String.join(",", paths);
            sb.append(pathValues).append('#');
        }

        // 拼接secret和时间戳
        sb.append("accessKeyId=")
                .append(accessKeyId)
                .append("#timestamp=").append(timestamp);
        return sb.toString();
    }

    public static void main(String[] args) {
        int defaultSize = 16;
        System.out.println("accessKeyId和accessKeySecret可以自由配置自定义字符串");
        System.out.println("建议accessKeyId用来区分企业，accessKeySecret可以随机生成");
        System.out.println("随机生成的字符串：" + randomStrings(defaultSize));
        System.out.println("随机生成的字符串：" + randomStrings(defaultSize));
        System.out.println("随机生成的字符串：" + randomStrings(defaultSize));
        System.out.println("随机生成的字符串：" + randomStrings(defaultSize));
        System.out.println("随机生成的字符串：" + randomStrings(defaultSize));
    }

    /**
     * 生成随机accessKeySecret
     *
     * @return String
     */
    private static String randomStrings(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

}