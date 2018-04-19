package org.linguisto.tools.imp.core.base;

/**
 */
public interface Processor {
	void setUp(ProcessingEngine engine) throws Exception;
    void process(Processable processable, ProcessingEngine engine) throws Exception;
	void tearDown() throws Exception;
}
