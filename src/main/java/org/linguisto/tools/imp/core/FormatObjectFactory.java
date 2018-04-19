package org.linguisto.tools.imp.core;

import org.linguisto.tools.imp.core.base.BaseObj;

/**
 */
public interface FormatObjectFactory {
    boolean isFormatObject(String field);
    boolean isTopLevelObject(String fieldName);
    BaseObj createObject(BaseObj parent, String field) throws Exception;
    void returnObject(BaseObj baseFormatObject) throws Exception;
}
