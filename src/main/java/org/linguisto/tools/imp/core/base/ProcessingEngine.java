package org.linguisto.tools.imp.core.base;


/**
 */
public interface ProcessingEngine {
    void addAttribute(String name, Object value);
    Object getAttribute(String name);
    void setAllUp() throws Exception;
    void process(Processable processable) throws Exception;
	void tearAllDown() throws Exception;
}
