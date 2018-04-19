package org.linguisto.tools.imp.core.converter;

import java.lang.annotation.Annotation;

/**
 */
public interface FieldsConverterExtension {
    Class<?> getAnnotationClass();
    Object convert(Annotation annotation, String value) throws Exception;
}
