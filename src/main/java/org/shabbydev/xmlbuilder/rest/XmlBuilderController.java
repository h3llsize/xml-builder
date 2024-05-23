package org.shabbydev.xmlbuilder.rest;

import org.shabbydev.xml.schemas.SmevBody;
import org.shabbydev.xmlbuilder.service.XmlBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/")
public class XmlBuilderController {

    @Autowired
    private XmlBuilderService xmlBuilderService;

    @PostMapping("build-xml")
    public ResponseEntity<String> buildXml(@RequestBody SmevBody smevBody) throws FileNotFoundException, JAXBException {
        return ResponseEntity.ok(xmlBuilderService.build(smevBody));
    }
}
