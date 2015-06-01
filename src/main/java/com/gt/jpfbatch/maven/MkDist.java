/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch.maven;

import java.awt.dnd.DragSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guillermot
 */
public class MkDist {

    public static void main(String... args) {
        try {

            File dist = new File("dist");

            if (dist.exists()) {
                Path directory = Paths.get("dist");

                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }

                });
            }

            dist.mkdirs();

            Files.copy(Paths.get("target/jpfbatch-1.0-SNAPSHOT-jar-with-dependencies.jar"), Paths.get("dist/jpfbatch.jar"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("src/main/resources/jpfbatch.properties"), Paths.get("dist/jpfbatch.properties"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("src/main/resources/log4j.production.properties"), Paths.get("dist/log4j.properties"), StandardCopyOption.REPLACE_EXISTING);

            /*
             Files.copy(Paths.get("jdbc"), Paths.get("dist/jdbc"), StandardCopyOption.REPLACE_EXISTING);
            
             File f = new File("jdbc");
            
             for(File fc : f.listFiles()) {
             Files.copy(Paths.get("jdbc/" + fc.getName()), Paths.get("dist/jdbc/" + fc.getName()), StandardCopyOption.REPLACE_EXISTING);
             }
            
             */
        } catch (IOException ex) {
            Logger.getLogger(MkDist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
