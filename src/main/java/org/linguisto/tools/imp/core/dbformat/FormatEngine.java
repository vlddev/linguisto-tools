package org.linguisto.tools.imp.core.dbformat;

import org.linguisto.tools.imp.core.ImportObject;
import org.linguisto.tools.imp.core.ImportObject;

public interface FormatEngine {
    void process(ImportObject ginfObject) throws Exception;
    void process(ImportObject ginfObject, boolean synchron) throws Exception;
    void update(ImportObject ginfObject) throws Exception;
    void processComplete() throws Exception;
    void close() throws Exception;
}