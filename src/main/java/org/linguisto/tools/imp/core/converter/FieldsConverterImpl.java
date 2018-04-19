package org.linguisto.tools.imp.core.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.converter.annotation.StringToDate;
import org.linguisto.tools.imp.core.converter.annotation.StringToInteger;
import org.linguisto.tools.imp.core.converter.annotation.StringToLong;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;
import org.linguisto.tools.imp.core.base.ErrorLevel;
import org.linguisto.tools.imp.core.converter.annotation.*;
import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.annotation.StringToLong;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;

public class FieldsConverterImpl implements FieldsConverter {
	public static final Logger log = Logger.getLogger(FieldsConverterImpl.class.getName());


	private static final SimpleDateFormat sdfDDMMYYYY = new SimpleDateFormat("ddMMyyyy");
	private static final SimpleDateFormat sdfDDMMYYYYHHMMSS = new SimpleDateFormat("ddMMyyyyHHmmss");
    private Map<String, DateFormat> formatCache = new HashMap<String, DateFormat>();
    private Map<Class<?>, FieldsConverterExtension> extensions = new HashMap<Class<?>, FieldsConverterExtension>();
    private List<FieldsConverterRule> converterRules;

    static {
    	sdfDDMMYYYY.setLenient(false);
    	sdfDDMMYYYYHHMMSS.setLenient(false);
    }

    public FieldsConverterImpl(List<FieldsConverterExtension> extensions, List<FieldsConverterRule> converterRules) {
    	if (extensions != null) {
            for (FieldsConverterExtension fieldsConverterExtension : extensions) {
                this.extensions.put(fieldsConverterExtension.getAnnotationClass(), fieldsConverterExtension);
            }
    	}
    	if (converterRules != null) {
            this.converterRules = converterRules;
    	} else {
            this.converterRules = new ArrayList<FieldsConverterRule>(1);
    	}
    }

    public void convert(BaseObj processable, Method method, String value) {
        try {
            if (value == null) return;
            value = value.trim();

            // Converter Rules aufrufen
            if (method.getAnnotation(StringToString.class)!= null && method.getParameterTypes()[0].equals(String.class)) {
            	StringToString stringToString = method.getAnnotation(StringToString.class);
                value = applyRules(StringToString.class, value);
                if (stringToString.toUppercase()) {
                	value = value.toUpperCase();
                }
                method.invoke(processable, value);
                return;
            }
            if (method.getAnnotation(StringToDate.class)!=null && method.getParameterTypes()[0].equals(Date.class)) {
                value = applyRules(StringToDate.class, value);

                StringToDate stringToDate = method.getAnnotation(StringToDate.class);
                DateFormat format = formatCache.get(stringToDate.format());
                try {
                	if (value != null && value.length() > 0) {
	                    if (format != null) {
	                        method.invoke(processable, format.parse(value));
	                        return;
	                    } else {
	                        format = new SimpleDateFormat(stringToDate.format());
	                        format.setLenient(false);
	                        formatCache.put(stringToDate.format(), format);
	                        method.invoke(processable, format.parse(value));
	                        return;
	                    }
                	}
                } catch (Throwable e) {
    				Method setterMethod = null;
					String setterName = stringToDate.formatErrorSetter();
					if (!"".equals(setterName)) {
						if (containsMethod(processable.getClass(),setterName)) {
							setterMethod = processable.getClass().getMethod(setterName, new Class[]{String.class});
							setterMethod.invoke(processable, value);
						}
					}
                    addError(processable, method, stringToDate.errorLevel(),
                    		"Unable to convert to date", value);
                }
            } else if (method.getAnnotation(StringToLong.class)!=null && (method.getParameterTypes()[0].equals(Long.class)|| method.getParameterTypes()[0].equals(Long.TYPE))) {
                value = applyRules(StringToLong.class, value);
                StringToLong stringToLong = method.getAnnotation(StringToLong.class);
                if (value != null) {
                    try {
                        Long aLong = null;
                        if (stringToLong.format().equals("")) {
                            if (!value.equals("")) {
                                aLong = Long.parseLong(value);
                            }
                        } else {
                            DecimalFormat decimalFormat = new DecimalFormat(stringToLong.format());
                            BigDecimal decimal = new BigDecimal(decimalFormat.parse(value).doubleValue());
                            aLong = decimal.setScale(0, stringToLong.roundingMode()).longValue();
                        }
                        method.invoke(processable, aLong);
                    } catch (Throwable e) {
                        addError(processable, method, method.getAnnotation(StringToLong.class).errorLevel(),
                        		"Unable to convert to long", value);
                    }
                }
            } else if (method.getAnnotation(StringToInteger.class)!=null && (method.getParameterTypes()[0].equals(Integer.class)|| method.getParameterTypes()[0].equals(Integer.TYPE))) {
                value = applyRules(StringToInteger.class, value);
                StringToInteger stringToInteger = method.getAnnotation(StringToInteger.class);
                if (value != null) {
                    try {
                        Integer integer = null;
                        if (stringToInteger.format().equals("")) {
                            if (!value.equals("")) {
                                integer = Integer.parseInt(value);
                            }
                        } else {
                            DecimalFormat decimalFormat = new DecimalFormat(stringToInteger.format());
                            BigDecimal decimal = new BigDecimal(decimalFormat.parse(value).doubleValue());
                            integer = decimal.setScale(0, stringToInteger.roundingMode()).intValue();
                        }
                        method.invoke(processable, integer);
                    } catch (Throwable e) {
                        addError(processable, method, method.getAnnotation(StringToInteger.class).errorLevel(),
                        		"Unable to convert to integer", value);
                    }
                }
            } else {
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (extensions.containsKey(annotation.annotationType())) {
                        value = applyRules(annotation.getClass(), value);
                        try {
                        	method.invoke(processable, extensions.get(annotation.annotationType()).convert(annotation, value));
                        } catch (Exception e) {
                            addError(processable, method, ErrorLevel.ERROR, e.getMessage(), value);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            addError(processable, method, ErrorLevel.ERROR, "An unknown converting error occured! ( " + processable.getClass()+ ")", value, e);
        }
    }

    private String applyRules(Class<?> annotation, String value) {
        for (int i = 0; i < converterRules.size(); i++) {
            FieldsConverterRule fieldsConverterRule = converterRules.get(i);
            value = fieldsConverterRule.applyRule(annotation, value);
        }
        return value;
    }
    
    private boolean containsMethod(Class<?> clasz, String methodName) {
        Method[] methods = clasz.getMethods();
        for (int i=0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                return true;
            }
        }
        return false;
     }

    
    private void addError(BaseObj processable, Method method, ErrorLevel errorLevel, String message, String value) {
        addError(processable, method, ErrorLevel.convertToLoggingLevel(errorLevel), "CONVERTING", message.toString(), value, null);
    }

    private void addError(BaseObj processable, Method method, ErrorLevel errorLevel, String message, String value, Exception e) {
        addError(processable, method, ErrorLevel.convertToLoggingLevel(errorLevel), "CONVERTING", message.toString(), value, e);
    }
    
    private void addError(BaseObj processable, Method method, Level errorLevel, String errorType, String message, String value, Exception e) {
        FormatField formatField = method.getAnnotation(FormatField.class);
        String msg = "[Obj: " + processable.getClass().getSimpleName() + " Field: " + formatField.name() + " ]"+ message;
        if (e != null) {
        	log.log(errorLevel, msg, e);
        } else {
        	log.log(errorLevel, msg);
        }
    }
}