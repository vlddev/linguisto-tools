package org.linguisto.tools.imp.core.base;

import java.util.List;

@SuppressWarnings("unchecked")
public interface ObjectFactory {
    List<Class> getSupportedClasses();
    Poolable borrowObject(Class objectClass) throws Exception;
    void returnObject(Poolable object) throws Exception;
    void returnObjects(List objectList) throws Exception;
}