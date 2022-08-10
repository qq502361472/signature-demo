package com.hjrpc.signature.aspect;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.hjrpc.signature.annoation.Signature;
import com.hjrpc.signature.constant.SignatureConstant;
import com.hjrpc.signature.exception.SignException;
import com.hjrpc.signature.inputstream.BodyReaderRequestWrapper;
import com.hjrpc.signature.properties.SignatureProperties;
import com.hjrpc.signature.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Aspect
@Slf4j
public class SignatureAspect {

    /**
     * 请求过期时间 10分钟
     */
    public static final int EXPIRE_TIME = 10 * 60 * 60;

    /**
     * 服务器误差时间 2分钟
     */
    public static final int ERROR_LIMIT = -2 * 60 * 60;

    /**
     * 验证重复请求30分钟后可再次调用
     */
    public static final int ONCE_EXPIRE_TIME = 30;

    private final Map<String, Map<String, String>> signatureAccessKeyGroupMap;
    private final RedisTemplate<String, Object> redisTemplate;

    public SignatureAspect(SignatureProperties signatureProperties, RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.signatureAccessKeyGroupMap = signatureProperties.getSecretGroup()
                .stream().collect(Collectors.toMap(SignatureProperties.AccessCodeEntity::getCode, SignatureProperties.AccessCodeEntity::getAccessKey));
    }

    @Around("execution(* com..controller..*.*(..)) " +
            "&& @annotation(signature) " +
            "&& (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping))"
    )
    public Object doAround(ProceedingJoinPoint pjp, Signature signature) throws Throwable {
        String signRedisKey = this.checkSign(StrUtil.isBlank(signature.signatureCode()) ? signature.value() : signature.signatureCode());
        Object proceed = pjp.proceed();
        redisTemplate.opsForValue().set(signRedisKey, StringUtils.EMPTY, ONCE_EXPIRE_TIME, TimeUnit.MINUTES);
        return proceed;
    }

    private String checkSign(String signatureCode) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String headAccessKeyId = request.getHeader(SignatureConstant.SIGNATURE_ACCESS_KEY_ID_KEY);
        String timestamp = request.getHeader(SignatureConstant.SIGNATURE_TIMESTAMP_KEY);
        String sign = request.getHeader(SignatureConstant.SIGNATURE_SIGN_KEY);
        // 系统中读取accessKeySecret
        Map<String, String> signatureAccessKeyMap = this.signatureAccessKeyGroupMap.get(signatureCode);
        // 系统中读取accessKeySecret
        String accessKeySecret = signatureAccessKeyMap.get(headAccessKeyId);
        if (StringUtils.isBlank(accessKeySecret)) {
            throw new SignException("验签失败，无效的accessKeyId");
        }
        // 校验请求是否重复
        if (Boolean.TRUE.equals(redisTemplate.hasKey(sign))) {
            throw new SignException("重复的请求");
        }
        // 校验签名的头信息是否合法
        checkAccessKeyHeaders(headAccessKeyId, signatureAccessKeyMap, timestamp, sign);
        //获取body（对应@RequestBody）
        String body = getBodyString(request);
        //获取parameters（对应@RequestParam）
        Map<String, String[]> params = getParamsMap(request);
        //获取path variable（对应@PathVariable）
        Collection<String> paths = getPaths(request);
        // 验证签名
        SignUtil.checkSign(body, params, paths, headAccessKeyId, accessKeySecret, Long.parseLong(timestamp), sign);
        return SignatureConstant.SIGNATURE_ACCESS_ONCE_PREFIX + sign;
    }

    private void checkAccessKeyHeaders(String headAccessKeyId, Map<String, String> signatureAccessKeyMap, String timestamp, String sign) {
        if (StringUtils.isAnyBlank(headAccessKeyId, timestamp, sign)) {
            throw new SignException("未获取到完整签名信息");
        }

        if (signatureAccessKeyMap == null || !signatureAccessKeyMap.containsKey(headAccessKeyId)) {
            throw new SignException("验证失败，错误的accessKeyId：" + headAccessKeyId);
        }
        long timestampLongVal;
        try {
            timestampLongVal = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new SignException("不支持的时间戳格式");
        }
        long l = System.currentTimeMillis() - timestampLongVal;
        // 允许服务求误差2分钟
        if (l < ERROR_LIMIT || l >= EXPIRE_TIME) {
            throw new SignException("请求签名已过期");
        }
    }

    private Collection<String> getPaths(HttpServletRequest request) {
        Collection<String> paths = null;
        ServletWebRequest webRequest = new ServletWebRequest(request, null);
        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVars = (Map<String, String>) webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (!CollectionUtils.isEmpty(uriTemplateVars)) {
            paths = uriTemplateVars.values();
        }
        return paths;
    }

    private Map<String, String[]> getParamsMap(HttpServletRequest request) {
        Map<String, String[]> params = null;
        if (!CollectionUtils.isEmpty(request.getParameterMap())) {
            params = request.getParameterMap();
        }
        return params;
    }

    private String getBodyString(HttpServletRequest request) throws IOException {
        String body = null;
        if (request instanceof BodyReaderRequestWrapper) {
            body = IoUtil.readUtf8(request.getInputStream());
        }
        return body;
    }
}