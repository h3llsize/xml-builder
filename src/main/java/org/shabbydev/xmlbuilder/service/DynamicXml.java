package org.shabbydev.xmlbuilder.service;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.shabbydev.xml.schemas.SmevSchemaAttribute;
import org.shabbydev.xml.schemas.SmevSchemaProperty;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DynamicXml implements IDynamicXml {

    private final SmevSchemaProperty smevSchemaProperty;
    private final DynamicJAXBContext dynamicJAXBContext;
    private DynamicEntity dynamic;
    private DynamicType type;

    public DynamicXml(SmevSchemaProperty smevSchemaProperty, DynamicJAXBContext dynamicJAXBContext) {
        this.smevSchemaProperty = smevSchemaProperty;
        this.dynamicJAXBContext = dynamicJAXBContext;

        this.generateDynamicType();

        dynamic = type.newDynamicEntity();

        this.fillAttributes();

        this.fillChilds();
    }

    public static DynamicXml of(SmevSchemaProperty schemaProperty, DynamicJAXBContext dynamicJAXBContext) {
        return new DynamicXml(schemaProperty, dynamicJAXBContext);
    }

    @Override
    public DynamicEntity get() {
        return dynamic;
    }

    private void generateDynamicType() {
        type = dynamicJAXBContext.getDynamicType(smevSchemaProperty.getName());

        if(type != null)
            return;

        type = dynamicJAXBContext.getDynamicType(smevSchemaProperty.getName() + "Type");

        if(type != null)
            return;

        throw new IllegalArgumentException("Dynamic type is not registered");
    }

    private void fillAttributes() {
        for (SmevSchemaAttribute attribute : smevSchemaProperty.getAttributes()) {
            try {
                String attributeName = getAttributeName(attribute.getName());

                if(attribute.getValue() != null)
                    if (!attribute.getValue().isEmpty())
                    tryToSetValue(attributeName, attribute.getValue());

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryToSetValue(String attrName, String value) {
        try {
            dynamic.set(attrName, value);
            return;
        } catch (Exception e) {
            System.out.println("BAD TRY TO SET VALUE WITH STRING");
        }

        try {
            dynamic.set(attrName, BigInteger.valueOf(Long.parseLong(value)));
            return;
        } catch (Exception e) {
            System.out.println("BAD TRY TO SET VALUE WITH BIG INTEGER");
        }

        try {
            dynamic.set(attrName, xmlGregorianCalendarFrom(value));
            return;
        } catch (ParseException | DatatypeConfigurationException ex) {
            System.out.println("BAD TRY TO SET VALUE WITH XMLGregorianCalendar");
        }
    }

    private XMLGregorianCalendar xmlGregorianCalendarFrom(String s) throws ParseException, DatatypeConfigurationException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(s);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    }

    private void fillChilds() {
        if(!smevSchemaProperty.isHasChildProperty())
            return;

        HashMap<String, List<Object>> identificalProperties = new HashMap<>();

        for (SmevSchemaProperty schemaProperty : smevSchemaProperty.getChild()) {
            if(!schemaProperty.isHasChildProperty() && schemaProperty.getAttributes().size() < 1 && schemaProperty.getValue() == null)
                continue;

            try {
                if(schemaProperty.isComplexType()) {
                    if(schemaProperty.getMaxOccurs() <= 1 && !schemaProperty.isUnbounded())
                        dynamic.set(getAttributeName(schemaProperty.getName()), DynamicXml.of(schemaProperty, dynamicJAXBContext).get());
                    else {
                        String attributeName = getAttributeName(schemaProperty.getName());

                        if(identificalProperties.containsKey(attributeName)) {
                            identificalProperties.get(attributeName).add(DynamicXml.of(schemaProperty, dynamicJAXBContext).get());
                        } else {
                            identificalProperties.put(attributeName, List.of(DynamicXml.of(schemaProperty, dynamicJAXBContext).get()));
                        }
                    }
                } else {
                    if(schemaProperty.getValue() != null)
                        if(schemaProperty.getMaxOccurs() <= 1 && !schemaProperty.isUnbounded())
                            tryToSetValue(getAttributeName(schemaProperty.getName()), schemaProperty.getValue());
                        else {
                            String attributeName = getAttributeName(schemaProperty.getName());

                            if(identificalProperties.containsKey(attributeName)) {
                                identificalProperties.get(attributeName).add(schemaProperty.getValue());
                            } else {
                                List<Object> strings = new ArrayList<>();
                                strings.add(schemaProperty.getValue());
                                identificalProperties.put(attributeName, strings);
                            }
                        }
                }

            } catch (IllegalArgumentException e) {
                if(schemaProperty.getValue() != null) {
                    dynamic.set(getAttributeName(schemaProperty.getName()), schemaProperty.getValue());
                }
            }
        }

        for (String s : identificalProperties.keySet()) {
            dynamic.set(s, identificalProperties.get(s));
        }
    }

    private String getAttributeName(String attributeName) {
        for (String propertiesName : type.getPropertiesNames()) {
            if(propertiesName.toLowerCase(Locale.ROOT).equals(attributeName.toLowerCase(Locale.ROOT))) {
                return propertiesName;
            }
        }

        throw new IllegalArgumentException("Dynamic attribute was not found");
    }
}
