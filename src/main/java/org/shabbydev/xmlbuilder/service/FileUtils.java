package org.shabbydev.xmlbuilder.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static List<Pair<Path, Path>> copyAndMoveFiles(String sourceFolderPath, String destinationFolderPath) {
        List<Pair<Path, Path>> copiedFiles = new ArrayList<>();

        File sourceFolder = new File(sourceFolderPath);
        File destinationFolder = new File(destinationFolderPath);

        if (sourceFolder.exists() && destinationFolder.exists() && sourceFolder.isDirectory() && destinationFolder.isDirectory()) {
            File[] filesToCopy = sourceFolder.listFiles();

            if (filesToCopy != null) {
                for (File file : filesToCopy) {
                    if(file.isDirectory()) {
                        File createdDirectory = new File(destinationFolderPath + "/" + file.getName());

                        if(createdDirectory.mkdir()) {
                            copyAndMoveFiles(file.getPath(), destinationFolderPath + "/" + file.getName());
                        }
                    } else {
                        File destinationFile = new File(destinationFolder, file.getName());
                        Path sourceFilePath = file.toPath();
                        Path destinationFilePath = destinationFile.toPath();

                        if (!destinationFile.exists()) {
                            try {
                                Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Скопирован файл: " + sourceFilePath.toString() + " -> " + destinationFilePath.toString());
                                copiedFiles.add(new Pair<>(sourceFilePath, destinationFilePath));
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Ошибка при копировании файла: " + sourceFilePath.toString());
                            }
                        } else {
                            System.out.println("Файл уже существует в конечной папке: " + sourceFilePath.toString());
                        }
                    }
                }
            }
        } else {
            System.err.println("Исходная или конечная папка не существует.");
        }

        return copiedFiles;
    }

    static class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
