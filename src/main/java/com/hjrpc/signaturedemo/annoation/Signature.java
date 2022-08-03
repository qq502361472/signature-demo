package com.hjrpc.signaturedemo.annoation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Signature {
    /**
     * 签名的配置代码
     */
    @AliasFor("signatureCode")
    String value() default "";

    /**
     * 签名的配置代码
     */
    @AliasFor("value")
    String signatureCode() default "";
}
