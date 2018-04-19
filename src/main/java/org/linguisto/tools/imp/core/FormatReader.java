package org.linguisto.tools.imp.core;

public interface FormatReader {    
    void readFile(String fileName) throws Exception;
    void addAttribute(String name, Object value);
    Object getAttribute(String name);
}
