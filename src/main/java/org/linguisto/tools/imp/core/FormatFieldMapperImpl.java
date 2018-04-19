package org.linguisto.tools.imp.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.FieldsConverter;

public final class FormatFieldMapperImpl implements FormatFieldMapper {

    private FieldsConverter fieldsConverter;
    private AnnotationCache annotationCache;

    private Map<Class<?>, Map<String, Method>> cache = new HashMap<Class<?>, Map<String, Method>>();

    public FormatFieldMapperImpl(AnnotationCache annotationCache, FieldsConverter fieldsConverter) {
        this.annotationCache = annotationCache;
        this.fieldsConverter = fieldsConverter;
    }

    public void mapObject(BaseObj processable, String fieldName, String value) {
        if (cache.get(processable.getClass())!= null && cache.get(processable.getClass()).get(fieldName)!=null) {
            convert(processable, fieldName, value);
        } else {
            Map<String, Method> mapChache = new HashMap<String, Method>();
            cache.put(processable.getClass(), mapChache);
            List<Method> methods = annotationCache.getAnnotatedMethods(processable.getClass(), FormatField.class);
            for (int i = 0; i < methods.size(); i++) {
                Method method1 = methods.get(i);
                FormatField formatField = method1.getAnnotation(FormatField.class);
                if (formatField!= null) {
                    mapChache.put(formatField.name(), method1);
                }
            }
            convert(processable, fieldName, value);
        }
    }

    private void convert(BaseObj processable, String name, String value) {
        Method method = cache.get(processable.getClass()).get(name);
        if (method != null) {
            fieldsConverter.convert(processable, method, value);
        }
    }

}
