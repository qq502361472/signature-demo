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
            ,RedisTemplate<String,Object> redisTemplate) {
        return new SignatureAspect(signatureProperties,redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }
}