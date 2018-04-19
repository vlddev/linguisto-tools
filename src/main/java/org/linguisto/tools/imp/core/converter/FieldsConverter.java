package org.linguisto.tools.imp.core.converter;

import java.lang.reflect.Method;

import org.linguisto.tools.imp.core.base.BaseObj;

public interface FieldsConverter {
    void convert(BaseObj processable, Method method, String value);
}
