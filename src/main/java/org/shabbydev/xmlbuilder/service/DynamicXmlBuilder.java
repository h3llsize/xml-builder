package org.shabbydev.xmlbuilder.service;

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.shabbydev.xml.schemas.SmevSchemaProperty;
import org.xml.sax.EntityResolver;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
        Marshaller marshaller = dynamicJAXBContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        marshaller.marshal(rootXml.get(), stringWriter);

        String xmlString = stringWriter.toString(); // Получить содержимое StringWriter в виде строки

        if(xmlString.contains("BapForPeriodRequest"))
            xmlString = xmlString.replaceAll("FEMALE", "Female").replaceAll("MALE", "Male")
                    .replaceAll("ns1", "smev").replaceAll("ns0", "tns")
                    .replaceAll("xmlns=\"http://kvs.pfr.com/benefits-common/1.0.0\"",
                            "xmlns:benefits=\"http://kvs.pfr.com/benefits-common/1.0.0\"")
                    .replaceAll("Snils", "benefits:Snils")
                    .replaceAll("BirthDate", "benefits:Birthdate")
                    .replaceAll("Gender", "benefits:Gender")
                    .replaceAll("BeginPeriod", "benefits:BeginPeriod")
                    .replaceAll("NumberOfMonths", "benefits:NumberOfMonths");
        if(xmlString.contains("RequestDL"))
            xmlString = xmlString.replaceAll("xmlns=\"urn://x-artefacts-rosreestr-gov-ru/virtual-services/realestate-info-for-person/1.0.1\"",
                    "xmlns:ns1=\"urn://x-artefacts-rosreestr-gov-ru/virtual-services/realestate-info-for-person/1.0.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                            .replaceAll("RequestDL", "ns1:RequestDL")
                            .replaceAll("Surname", "ns1:Surname")
                            .replaceAll("Firstname", "ns1:Firstname")
                            .replaceAll("Patronymic", "ns1:Patronymic")
                            .replaceAll("Birthdate", "ns1:Birthdate")
                            .replaceAll("Birthplace", "ns1:Birthplace")
                            .replaceAll("INN", "ns1:INN")
                            .replaceAll("SNILS", "ns1:SNILS")
                            .replaceAll("Document", "ns1:Document")
                            .replaceAll("Series", "ns1:Series")
                            .replaceAll("Number", "ns1:Number")
                            .replaceAll("Date", "ns1:Date");

        System.out.println(xmlString); // Печатаем строку с XML

        return xmlString;
    }
}
