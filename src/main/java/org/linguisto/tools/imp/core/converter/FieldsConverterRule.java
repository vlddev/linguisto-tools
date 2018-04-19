package org.linguisto.tools.imp.core.converter;

/**
 */
public interface FieldsConverterRule {
    String applyRule(Class<?> annotation, String value);
}
