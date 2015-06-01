/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch.maven;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guillermot
 */
public class VersionIncrementer {

    public static void main(String... args) {
        try {
            java.util.Properties p = new java.util.Properties();
            p.load(new java.io.FileInputStream("src/main/resources/version.properties"));
            int version = Integer.valueOf(p.getProperty("BUILD"));
            version++;
            p.setProperty("BUILD", version + "");
            p.store(new java.io.FileOutputStream("src/main/resources/version.properties"), null);
            
            Logger.getLogger(VersionIncrementer.class.getName()).log(Level.INFO, "Version incrementada a " + version);
        } catch (IOException ex) {
            Logger.getLogger(VersionIncrementer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
