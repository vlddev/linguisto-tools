package org.linguisto.tools.imp.core.base;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementiert die ProcessingEngine
 * Diese Klasse ist *nicht* threadsafe !!
 *
 * @see de.gema.imex.importer.core.base.ProcessingEngine
 */
public final class ProcessingEngineImpl implements ProcessingEngine {

    private static final Logger log = Logger.getLogger(ProcessingEngineImpl.class.getName());

    private List<Processor> processors = null;
    private Map<String, Object> attributes = null;
    private int processId = 0;

    public ProcessingEngineImpl(List<Processor> processors) {
        this.processors = processors;
        attributes = new HashMap<String, Object>();
    }

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAllUp() throws Exception {
    	for (Processor proc : processors)
    		proc.setUp(this);
    }

    public void process(Processable processable) throws Exception {
        try {
            int id = processId++;
            if (id == processors.size()) {
                return;
            }
            Processor processor = processors.get(id);
            processor.process(processable, this);
        } catch (Throwable th) {
            log.log(Level.SEVERE, "An unexpected error occurred while processing!", th);
            throw new Exception(th);
        } finally {
            processId = 0;            
        }
    }
    
    public void tearAllDown() throws Exception {
    	for (Processor proc : processors)
    		proc.tearDown();
    }
}