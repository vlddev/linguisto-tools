package org.linguisto.tools.imp.core.base;

import java.io.Serializable;

/**
 */
public interface Poolable extends Serializable {
    boolean inUse();
    void activate();
    void reset();
    boolean validate();
}
