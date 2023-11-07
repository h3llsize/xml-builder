package org.shabbydev.xmlbuilder.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class MyEntityResolver implements EntityResolver {
    private final String myFilePath;

    public MyEntityResolver(String myFilePath) {
        this.myFilePath = myFilePath;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws FileNotFoundException {
        String filename = new File(systemId).getName();
        if (systemId.contains("commons")) {
            String correctedId = myFilePath + "commons/" + filename;

            InputSource is = new InputSource(ClassLoader.getSystemResourceAsStream(correctedId));
            is.setSystemId(correctedId);
            is.setPublicId(publicId);
            is.setByteStream(new FileInputStream(correctedId));

            return is;
        }

        return null;
    }
}
