package org.linguisto.tools.imp.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class AnnotationCacheImpl implements AnnotationCache {
    private Map<Class, Map<Class, List<Method>>> map = new HashMap<Class, Map<Class, List<Method>>>();

    public List<Method> getAnnotatedMethods(Class annotatedClass, Class methodAnnotation) {
        if (map.get(annotatedClass)!= null && map.get(annotatedClass).get(methodAnnotation) != null) {
            return map.get(annotatedClass).get(methodAnnotation);
        } else {
            if (map.get(annotatedClass) == null) map.put(annotatedClass, new HashMap<Class, List<Method>>());
            Method[] methods = annotatedClass.getMethods();
            List<Method> list = new ArrayList<Method>();
            for (Method method : methods) {
                Annotation annotation = method.getAnnotation(methodAnnotation);
                if (annotation != null) {
                    list.add(method);
                }
            }
            map.get(annotatedClass).put(methodAnnotation, list);
            return list;
        }
    }
}