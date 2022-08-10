package com.hjrpc.signature.config;

import com.hjrpc.signature.aspect.SignatureAspect;
import com.hjrpc.signature.filter.RequestCachingFilter;
import com.hjrpc.signature.properties.SignatureProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureConfig {

    @Bean
    @ConditionalOnMissingBean
    public RequestCachingFilter requestCachingFilter() {
        return new RequestCachingFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SignatureAspect signatureAspect(SignatureProperties signatureProperties
            , StringRedisTemplate stringRedisTemplate) {
        return new SignatureAspect(signatureProperties, stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        stringRedisTemplate.setKeySerializer(RedisSerializer.string());
        stringRedisTemplate.setValueSerializer(RedisSerializer.json());
        return stringRedisTemplate;
    }
}