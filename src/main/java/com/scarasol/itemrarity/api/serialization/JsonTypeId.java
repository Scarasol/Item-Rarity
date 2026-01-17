package com.scarasol.itemrarity.api.serialization;

import java.lang.annotation.*;

/**
 * 用来获取json序列化ID的注解
 * @author Scarasol
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonTypeId {
    String value();
}
