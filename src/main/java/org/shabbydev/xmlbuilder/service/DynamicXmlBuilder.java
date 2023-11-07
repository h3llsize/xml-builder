package org.shabbydev.xmlbuilder.service;

import jakarta.xml.bind.JAXBException;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.shabbydev.xml.schemas.SmevSchemaProperty;
import org.xml.sax.EntityResolver;

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
        DynamicJAXBContext dynamicJAXBContext = DynamicJAXBContextFactory
                .createContextFromXSD(new FileInputStream(xsdFilePath), null, ClassLoader.getSystemClassLoader(), null);

        return new DynamicXmlBuilder(dynamicJAXBContext);
    }

    public static DynamicXmlBuilder of(String xsdFilePath, EntityResolver entityResolver) throws FileNotFoundException, JAXBException {
        DynamicJAXBContext dynamicJAXBContext = DynamicJAXBContextFactory
                .createContextFromXSD(new FileInputStream(xsdFilePath), entityResolver, ClassLoader.getSystemClassLoader(), null);

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
