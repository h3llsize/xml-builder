package org.shabbydev.xmlbuilder.service;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.shabbydev.xml.schemas.SmevSchemaAttribute;
import org.shabbydev.xml.schemas.SmevSchemaProperty;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
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
        if(smevSchemaProperty.getNamespace().equals("urn://x-artefacts-rosreestr-gov-ru/virtual-services/realestate-info-for-person/1.0.1") &&
                smevSchemaProperty.getName().equals("Document")) {
            type = dynamicJAXBContext.getDynamicType("ru.rtlabs.nsud.smev.metadata.datamart.RequestDL.Document");
            return;
        }

        type = dynamicJAXBContext.getDynamicType(smevSchemaProperty.getName());

        if(type != null)
            return;

        type = dynamicJAXBContext.getDynamicType(smevSchemaProperty.getTypeName());

        if(type != null)
            return;

        String old = smevSchemaProperty.getTypeName();
        String capitalized = old.substring(0, 1).toUpperCase() + old.substring(1);

        type = dynamicJAXBContext.getDynamicType(capitalized);

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

        if ("senderType".equals(attrName)) {
            try {
                dynamic.set(attrName, dynamicJAXBContext.getEnumConstant("ru.gov.rosreestr.artefacts.x.virtual_services.egrn_statement._1_1.SenderTypes", value));
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("BAD TRY TO SET VALUE WITH ENUM");
            } catch (JAXBException e) {
                System.out.println("BAD TRY TO SET VALUE WITH ENUM");
            }
        }

        if("gender".equals(attrName)) {
            try {
                dynamic.set(attrName, dynamicJAXBContext.getEnumConstant("ru.gov.smev.artefacts.x.supplementary.commons._1_0.GenderType", value.toUpperCase()));
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("BAD TRY TO SET VALUE WITH ENUM");
            } catch (JAXBException e) {
                System.out.println("BAD TRY TO SET VALUE WITH ENUM");
            }

        }

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
            dynamic.set(attrName, Integer.valueOf(value));
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

        try {
            dynamic.set(attrName, Boolean.valueOf(value));
            return;
        } catch (Exception e) {
            System.out.println("BAD TRY TO SET VALUE WITH BOOLEAN");
        }
    }

    private XMLGregorianCalendar xmlGregorianCalendarFrom(String s) throws ParseException, DatatypeConfigurationException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(s);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal.get(GregorianCalendar.YEAR),
                cal.get(GregorianCalendar.MONTH) + 1,  // Months are 0-based in GregorianCalendar
                cal.get(GregorianCalendar.DAY_OF_MONTH),
                DatatypeConstants.FIELD_UNDEFINED,  // Hour
                DatatypeConstants.FIELD_UNDEFINED,  // Minute
                DatatypeConstants.FIELD_UNDEFINED,  // Second
                DatatypeConstants.FIELD_UNDEFINED,  // Millisecond
                DatatypeConstants.FIELD_UNDEFINED   // Timezone
        );
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
                    if(schemaProperty.getMaxOccurs() <= 1 && !schemaProperty.isUnbounded()) {
                        if (schemaProperty.isHasChildProperty() || schemaProperty.getValue() != null || hasAttributeValue(schemaProperty))
                            dynamic.set(getAttributeName(schemaProperty.getName()), DynamicXml.of(schemaProperty, dynamicJAXBContext).get());
                    }
                    else {
                        String attributeName = getAttributeName(schemaProperty.getName());

                        if(identificalProperties.containsKey(attributeName)) {
                            identificalProperties.get(attributeName).add(DynamicXml.of(schemaProperty, dynamicJAXBContext).get());
                        } else {
                            List<Object> arrayList = new ArrayList<>();
                            arrayList.add(DynamicXml.of(schemaProperty, dynamicJAXBContext).get());

                            identificalProperties.put(attributeName, arrayList);
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

    private boolean hasAttributeValue(SmevSchemaProperty smevSchemaProperty) {
        for (SmevSchemaAttribute attribute : smevSchemaProperty.getAttributes()) {
            if(attribute.getValue() != null && !attribute.getValue().isEmpty())
                return true;
        }

        return false;
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
