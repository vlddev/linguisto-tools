package org.linguisto.tools.imp.core.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unchecked")
public final class ObjectFactoryImpl implements ObjectFactory {

    protected final Map<Class, List<Poolable>> poolFactories = new HashMap<Class, List<Poolable>>();

    public ObjectFactoryImpl(List<String> objects) throws Exception {
        for (String s : objects) {
            Class clazz = Class.forName(s);
            poolFactories.put(clazz, new LinkedList<Poolable>());
        }
    }

    public List<Class> getSupportedClasses() {
        List<Class> list = new ArrayList<Class>();
        list.addAll(poolFactories.keySet());
        return list;
    }

    public Poolable borrowObject(Class objectClass) throws Exception {
        if (objectClass.isInterface()) {
            objectClass = ClassLoader.getSystemClassLoader().loadClass(new StringBuffer(objectClass.getName()).append("Impl").toString());
        }
        Poolable poolPoolable;
        List<Poolable> list = poolFactories.get(objectClass);
        synchronized (list) {
            if (list.size() == 0) {
                poolPoolable = (Poolable)objectClass.newInstance();
            } else {
                poolPoolable = list.remove(0);
        	}
        }
        poolPoolable.activate();
        return poolPoolable;
    }

    public void returnObject(Poolable object) throws Exception {
        object.reset();
        List<Poolable> list = poolFactories.get(object.getClass());
        synchronized (list) {
            list.add(object);
        }
    }

    public void returnObjects(List objectList) throws Exception {
        if (objectList.size() > 0) {
            List<Poolable> list = poolFactories.get(objectList.get(0).getClass());
            for (int i = 0; i < objectList.size(); i++) {
                Poolable poolable = (Poolable)objectList.get(i);
                poolable.reset();
            }
            synchronized (list) {
                list.addAll(objectList);
            }
        }
    }
}
