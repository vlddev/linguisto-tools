package org.linguisto.tools.imp.core.converter.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.RoundingMode;

import org.linguisto.tools.imp.core.base.ErrorLevel;

/**
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringToInteger {    
    ErrorLevel errorLevel() default ErrorLevel.WARNING;
    String format() default "";
    RoundingMode roundingMode() default RoundingMode.UNNECESSARY;    
}
