package org.shabbydev.xmlbuilder.service;

import jakarta.xml.bind.JAXBException;
import org.shabbydev.xml.schemas.SmevSchemaProperty;

public interface IDynamicXmlBuilder {
    String build() throws JAXBException;
    DynamicXmlBuilder with(SmevSchemaProperty smevSchemaProperty);
}
