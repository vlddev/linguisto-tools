package org.linguisto.tools.imp.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.linguisto.tools.imp.core.annotation.ContainsFormatChild;
import org.linguisto.tools.imp.core.annotation.FormatChild;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.annotation.FormatParent;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.base.ObjectFactory;

@SuppressWarnings("unchecked")
public final class FormatObjectFactoryImpl implements FormatObjectFactory {
	public static final Logger log = Logger.getLogger(FormatObjectFactoryImpl.class.getName());

    private ObjectFactory objectFactory;
    private AnnotationCache annotationCache;
    private Map<String, Class> fieldToClass = new HashMap<String, Class>();
    private Map<String, Boolean> fieldToTopLevel = new HashMap<String, Boolean>();

    public FormatObjectFactoryImpl(ObjectFactory objectFactory, AnnotationCache annotationCache) {
        this.objectFactory = objectFactory;
        this.annotationCache = annotationCache;
        List<Class> objectClasses = objectFactory.getSupportedClasses();
        for (int i = 0; i < objectClasses.size(); i++) {
            Class aClass = objectClasses.get(i);
            FormatObject formatObject = (FormatObject)aClass.getAnnotation(FormatObject.class);
            if (formatObject != null) {
                fieldToClass.put(formatObject.name(), aClass);
                fieldToTopLevel.put(formatObject.name(), formatObject.topLevel());
            }
        }
    }

    public boolean isFormatObject(String fieldName) {
        return fieldToClass.containsKey(fieldName);
    }

    public boolean isTopLevelObject(String fieldName) {
        return fieldToTopLevel.get(fieldName);
    }

    public BaseObj createObject(BaseObj parent, String fieldName) throws Exception {
        if (isFormatObject(fieldName)) {
            BaseObj baseFormatObject = (BaseObj)objectFactory.borrowObject(fieldToClass.get(fieldName));
            if (parent != null) {
                linkWithChild(parent, fieldName, baseFormatObject);
                recurseChilds(parent, fieldName, baseFormatObject);
                linkWithParent(parent, baseFormatObject);
            }
            return baseFormatObject;
        }
        throw new RuntimeException(fieldName+" has no object");
    }

    private void linkWithParent(BaseObj parent, BaseObj baseFormatObject) {
        List<Method> methods = annotationCache.getAnnotatedMethods(baseFormatObject.getClass(), FormatParent.class);
        if (methods.size()>0) {
            for (Method method : methods) {
                try {
                    method.invoke(baseFormatObject, parent);
                } catch (Exception e) {
    				log.log(Level.SEVERE, "Error occurred ->" + method, e);
                }
            }
        }
    }

    private void recurseChilds(BaseObj parent, String fieldName, BaseObj baseFormatObject) throws Exception {
        List<Method> methods = annotationCache.getAnnotatedMethods(parent.getClass(), ContainsFormatChild.class);
        for (Method method : methods) {
            Object result = null;
            result = method.invoke(parent);

            if (result == null)
                continue;
            else if (result instanceof List) {
                List<BaseObj> list = (List)result;

                if (list.size() > 0 && list.get(0) instanceof BaseObj)
                    linkWithChild(list.get(0), fieldName, baseFormatObject);

                for (BaseObj child : list)
                    recurseChilds(child, fieldName, baseFormatObject);

            }
            else if (result instanceof BaseObj)
                linkWithChild((BaseObj)result, fieldName, baseFormatObject);
        }
    }

    private void linkWithChild(BaseObj parent, String fieldName, BaseObj baseFormatObject) {
        List<Method> methods = annotationCache.getAnnotatedMethods(parent.getClass(), FormatChild.class);
        if (methods.size()>0) {
            for (int i = 0; i < methods.size(); i++) {
                Method method = methods.get(i);
                FormatChild formatChild = method.getAnnotation(FormatChild.class);
                if (formatChild.name().equals(fieldName)) {
                    try {
                        method.invoke(parent, baseFormatObject);
                    } catch (Exception e) {
        				log.log(Level.SEVERE, "Could not link format object with childs. Check your annotations.", e);
                    }
                }
            }
        }
    }

    public void returnObject(BaseObj baseFormatObject) throws Exception {
        objectFactory.returnObject(baseFormatObject);
    }
}