package com.hjrpc.signaturedemo.config;

import com.hjrpc.signaturedemo.aspect.SignatureAspect;
import com.hjrpc.signaturedemo.filter.RequestCachingFilter;
import com.hjrpc.signaturedemo.properties.SignatureProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

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
    public RedisTemplate<String,Object> redisTemplate(){
        return new RedisTemplate<>();
    }
}