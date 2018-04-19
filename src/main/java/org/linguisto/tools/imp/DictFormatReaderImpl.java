package org.linguisto.tools.imp;

import org.linguisto.tools.imp.core.FormatFieldMapper;
import org.linguisto.tools.imp.core.FormatObjectFactory;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.linguisto.tools.imp.core.FormatFieldMapper;
import org.linguisto.tools.imp.core.FormatObjectFactory;
import org.linguisto.tools.imp.core.FormatReader;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.base.ProcessingEngine;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.OutputStream;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

public final class DictFormatReaderImpl extends DefaultHandler implements FormatReader {
	public static final Logger log = Logger.getLogger(DictFormatReaderImpl.class.getName());

    String SOFT_HYPHEN_STRING = "\u00AD";

    private StringBuffer nodeValue = new StringBuffer();
    private ProcessingEngine processingEngine;
    private FormatObjectFactory formatObjectFactory;
    private FormatFieldMapper formatFieldMapper;
    private final Stack<BaseObj> stack = new Stack<BaseObj>();
    private Map<String, Object> attributes = null;

    public DictFormatReaderImpl(
            ProcessingEngine processingEngine,
            FormatObjectFactory formatObjectFactory,
            FormatFieldMapper formatFieldMapper) {
        this.processingEngine = processingEngine;
        this.formatObjectFactory = formatObjectFactory;
        this.formatFieldMapper = formatFieldMapper;
        attributes = new HashMap<String, Object>();
    }

    public void readFile(String fileName) throws Exception {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            System.gc();
            processingEngine.setAllUp();
            parserFactory.newSAXParser().parse(new File(fileName), this);
            processingEngine.tearAllDown();
            System.gc();
        } catch (SAXParseException spe) {
            throw new Exception("Error while parsing xml file at line "+spe.getLineNumber(), spe);
        } catch (SAXException e) {
            throw new Exception("Error while parsing file.", e);
        } catch (IOException e) {
            throw new Exception("Error while opening/reading file.", e);
        } catch (ParserConfigurationException e) {
            throw new Exception("Error while configurating parser.", e);
        }
    }

    public void validateXml(String fileName, String dtdFile) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdFile);
        //ignore output
        OutputStream nullOutputStream = new OutputStream() { @Override public void write(int b) { } };
        transformer.transform(new StreamSource(fileName), new StreamResult(nullOutputStream));
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	if (formatObjectFactory.isFormatObject(qName)) {
            try {
                BaseObj parent = null;
                if (stack.size()>0) {
                    parent = stack.peek();
                }
                BaseObj newObj = formatObjectFactory.createObject(parent, qName);
				for (int i = 0; i < attributes.getLength(); i++) {
					String value = attributes.getValue(i).trim();
                    //TODO replace invisible chars
                    if (value.contains(SOFT_HYPHEN_STRING)) {
                        log.warning("SOFT_HYPHEN will be replaced in '" + value + "'");
                        value = value.replace(SOFT_HYPHEN_STRING, "");
                    }
                    formatFieldMapper.mapObject(newObj, attributes.getQName(i), value);
				}
                stack.push(newObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
            	BaseObj parent = null;
                if (attributes.getLength() > 0 && stack.size()>0) {
                    parent = stack.peek();
                    if (parent != null) {
        				for (int i = 0; i < attributes.getLength(); i++) {
        					String value = attributes.getValue(i).trim();
        					formatFieldMapper.mapObject(parent, qName+"."+attributes.getQName(i), value);
        				}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (stack.size()>0) {
        	BaseObj baseFormatObject = stack.peek();
            if (formatObjectFactory.isFormatObject(qName)) {
                baseFormatObject = stack.pop();
                String value = nodeValue.toString().replaceAll("[\n\t\r\u0000]", "").trim();
                formatFieldMapper.mapObject(baseFormatObject, "TAG-VALUE", value);
                if (formatObjectFactory.isTopLevelObject(qName)) {
                    try {
                        processingEngine.process(baseFormatObject);
                    } catch (Exception e) {
                        throw new SAXException("Error while processing xml", e);
                    } finally {
                        try {
                            formatObjectFactory.returnObject(baseFormatObject);
                        } catch (Exception e) {
            				log.log(Level.SEVERE, "Error occurred. Tag - " + qName, e);
                        }
                    }
                }
            } else {
                String value = nodeValue.toString().replaceAll("[\n\t\r\u0000]", "").trim();
                formatFieldMapper.mapObject(baseFormatObject, qName, value);
            }
        }
        nodeValue = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    	nodeValue.append(String.valueOf(ch, start, length));
    }

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}