package org.shabbydev.xmlbuilder.service;

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.shabbydev.xml.schemas.SmevSchemaProperty;
import org.xml.sax.EntityResolver;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;

public class DynamicXmlBuilder implements IDynamicXmlBuilder{
    private final DynamicJAXBContext dynamicJAXBContext;
    private DynamicXml rootXml;

    public DynamicXmlBuilder(DynamicJAXBContext dynamicJAXBContext) {
        this.dynamicJAXBContext = dynamicJAXBContext;
    }

    private DynamicXmlBuilder() {
        this.dynamicJAXBContext = null;
        //no instance
    }

    public static DynamicXmlBuilder of(String xsdFilePath) throws FileNotFoundException, JAXBException {
        System.out.println("Создаем XSD для : " + xsdFilePath);

        DynamicJAXBContext dynamicJAXBContext = DynamicJAXBContextFactory
                .createContextFromXSD(new FileInputStream(xsdFilePath), null, null, null);

        return new DynamicXmlBuilder(dynamicJAXBContext);
    }

    public static DynamicXmlBuilder of(String xsdFilePath, EntityResolver entityResolver) throws FileNotFoundException, JAXBException {
        DynamicJAXBContext dynamicJAXBContext = DynamicJAXBContextFactory
                .createContextFromXSD(new FileInputStream(xsdFilePath), entityResolver, null, null);

        return new DynamicXmlBuilder(dynamicJAXBContext);
    }
    public DynamicXmlBuilder with(SmevSchemaProperty rootProperty) {
        fillDynamicFields(rootProperty);
        return this;
    }

    private void fillDynamicFields(SmevSchemaProperty smevSchemaProperty) {
        this.rootXml = DynamicXml.of(smevSchemaProperty, dynamicJAXBContext);
    }

    public String build() throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        dynamicJAXBContext.createMarshaller().marshal(rootXml.get(), stringWriter);

        return stringWriter.toString();
    }
}
