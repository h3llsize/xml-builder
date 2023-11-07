package org.shabbydev.xmlbuilder.service;

import jakarta.xml.bind.JAXBException;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.shabbydev.xml.schemas.SmevBody;
import org.shabbydev.xml.schemas.SmevSchemaAttribute;
import org.shabbydev.xml.schemas.SmevSchemaProperty;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class XmlBuilderService {
    public String build(SmevBody smevBody) throws FileNotFoundException, JAXBException {
        copyCommons(smevBody.getXsdFilePath());

        return DynamicXmlBuilder.of(smevBody.getXsdFilePath()).with(smevBody.getSmevSchemaProperty()).build();
    }

    private String getFilePath(String inputString) {
        int lastIndex = inputString.lastIndexOf('/');

        if (lastIndex != -1) {
            return inputString.substring(0, lastIndex + 1);
        } else {
            throw new IllegalArgumentException("Плохой путь к файлу");
        }
    }

    private void copyCommons(String xsdFilePath) {
        String filePath = getFilePath(xsdFilePath);
        copyAndMoveFiles(filePath + "commons/", System.getProperty("user.dir") + "/commons/");
    }

    private void copyAndMoveFiles(String sourceFolderPath, String destinationFolderPath) {
        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);

        if (sourceFolder.exists() && destinationFolder.exists() && sourceFolder.isDirectory() && destinationFolder.isDirectory()) {
            File[] filesToCopy = sourceFolder.listFiles();

            if (filesToCopy != null) {
                for (File file : filesToCopy) {
                    File destinationFile = new File(destinationFolder, file.getName());

                    if (!destinationFile.exists()) {
                        try {
                            Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Скопирован файл: " + file.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Ошибка при копировании файла: " + file.getName());
                        }
                    } else {
                        System.out.println("Файл уже существует в конечной папке: " + file.getName());
                    }
                }
            }
        } else {
            System.err.println("Исходная или конечная папка не существует.");
        }
    }

    private DynamicEntity fillDynamicEntityFields(DynamicEntity dynamic, SmevSchemaProperty root, DynamicJAXBContext dynamicJAXBContext) {
        if(!root.isComplexType()) {
            dynamic.set(root.getName(), root.getValue());

            return dynamic;
        } else {
            return fillDynamicComplexType(dynamic, root, dynamicJAXBContext);
        }
    }

    private DynamicEntity fillDynamicComplexType(DynamicEntity dynamic, SmevSchemaProperty property, DynamicJAXBContext dynamicJAXBContext) {
        fillAttributes(dynamic, property);

        for (SmevSchemaProperty child : property.getChild()) {

            String childEntityName = null;
            DynamicEntity childEntity = null;
            boolean cannotBeEntity = false;

            try {
                childEntity = dynamicJAXBContext.newDynamicEntity(child.getName());
                childEntityName = child.getName();
            } catch (Exception e) {
                try {
                    childEntity = dynamicJAXBContext.newDynamicEntity(child.getName() + "Type");
                    childEntityName = child.getName() + "Type";
                } catch (Exception ignored) {
                    cannotBeEntity = true;
                }
            }
            if(!cannotBeEntity) {
                fillDynamicComplexType(childEntity, child, dynamicJAXBContext);

                if (child.isComplexType()) {
                    dynamic.set(getPropertiesName(child.getName()), childEntity);
                } else {
                    fillAttributes(childEntity, child);
                    dynamic.set(getPropertiesName(child.getName()), childEntity);
                    return dynamic;
                }
            } else {
                if(child.getValue() != null)
                    dynamic.set(getPropertiesName(child.getName()), child.getValue());
            }
        }

        return dynamic;
    }

    private void fillAttributes(DynamicEntity dynamic, SmevSchemaProperty property) {
        for (SmevSchemaAttribute attribute : property.getAttributes()) {
            if(attribute.getValue() != null)
                dynamic.set(getPropertiesName(attribute.getName()), property.getValue());
        }
    }

    public String getPropertiesName(String myName) {
        return "";
    }
}
