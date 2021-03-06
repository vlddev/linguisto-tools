package org.linguisto.tools.imp.core.converter.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.linguisto.tools.imp.core.base.ErrorLevel;

/**
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringToDate {
    ErrorLevel errorLevel() default ErrorLevel.WARNING;
    String format();
    String formatErrorSetter() default "";
}
