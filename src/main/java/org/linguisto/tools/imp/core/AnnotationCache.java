package org.linguisto.tools.imp.core;

import java.lang.reflect.Method;
import java.util.List;

public interface AnnotationCache {
    List<Method> getAnnotatedMethods(Class<?> annotatedClass, Class<?> methodAnnotation);
}