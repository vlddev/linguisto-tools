package org.linguisto.tools.imp.core.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormatObject {
    String name();
    boolean topLevel() default false;
    boolean prefixLang() default true;
}
