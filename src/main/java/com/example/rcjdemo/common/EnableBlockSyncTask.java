package com.example.rcjdemo.common;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @className EnableBlockSyncTask
 * @author lhc
 * @date 2022年07月13日 09:57
 * @description 描述
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({BlockSync.class})
public @interface EnableBlockSyncTask {
}
