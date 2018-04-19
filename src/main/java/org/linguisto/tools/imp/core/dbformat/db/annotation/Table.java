package org.linguisto.tools.imp.core.dbformat.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name();
    String seq() default "";
    int insertSize() default 1;
    DBWriterType dbWriterType() default DBWriterType.STANDARD;
    int dbWriterLevel() default 1;
}