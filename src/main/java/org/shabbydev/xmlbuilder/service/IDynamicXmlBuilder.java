package org.shabbydev.xmlbuilder.service;

import org.shabbydev.xml.schemas.SmevSchemaProperty;

import javax.xml.bind.JAXBException;

public interface IDynamicXmlBuilder {
    String build() throws JAXBException;
    DynamicXmlBuilder with(SmevSchemaProperty smevSchemaProperty);
}
