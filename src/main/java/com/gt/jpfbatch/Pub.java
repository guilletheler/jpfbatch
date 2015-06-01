/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch;

import com.gcem.iap.IniParametersFormatter;
import com.gcem.iap.Parameters;
import com.gt.ifepson.IfCommand;
import com.gt.ifepson.capa_fisica.OutParam;
import com.gt.ifepson.capa_fisica.InParam;
import com.gt.ifepson.capa_fisica.serialPort.PortConfig;
import com.gt.ifepson.fiscaldoc.FiscalDocument;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author guillermot
 */
public class Pub {

    //protected static EnvVars env = null;
    //protected static SerialPort serialPort = null;
    public static final String envFile = "jpfbatch.properties";
    static Parameters parameters;
    
    public static void init(String[] args) throws Exception {
        
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        String jarDir = jarFile.getParentFile().getPath();
        
        File f;
        
        f = new File(jarDir + "/log4j.properties");
        
        if (!f.exists()) {
            f = new File("/home/prog/java_mvn/jpfbatch/src/main/resources/log4j.properties");
        }

        //PropertiesConfigurator is used to configure logger from properties file
        PropertyConfigurator.configure(f.getAbsolutePath());
        
        IniParametersFormatter ini = new IniParametersFormatter();
        
        File paramsFile = new File(jarDir + "/jpfbatch.properties");
        
        if (!paramsFile.exists()) {
            paramsFile = new File("/home/prog/java_mvn/jpfbatch/src/main/resources/jpfbatch.properties");
        }
        
        parameters = ini.load(paramsFile);
        
        Pub.addOptions();
        
        parameters.parseArgs(args);
        
    }
    
    public static void showHelp(String commandName) {
        showHelp(commandName, true);
    }
    
    public static void showHelp(String commandName, boolean completo) {
        Class c = FiscalDocument.getComandosDisponibles().get(commandName.toUpperCase());
        
        if (c != null) {
            
            Object obj = null;
            try {
                Class<?> clazz = Class.forName(c.getName());
                
                obj = clazz.newInstance();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Pub.class.getName()).log(Level.FATAL, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Pub.class.getName()).log(Level.FATAL, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Pub.class.getName()).log(Level.FATAL, null, ex);
            }
            
            if (obj != null) {
                
                IfCommand com = (IfCommand) obj;
                
                if (completo) {
                    System.out.println("\n Ayuda de comando fiscal " + com.getNombreAbreviado());
                    System.out.println("-------------------------");
                    System.out.println(org.apache.commons.lang3.StringUtils.leftPad("", com.getNombreAbreviado().length(), '-'));
                    System.out.println(com.getDescripcionComando());
                    
                    Collections.sort(com.getPosiblesParams(), new Comparator<InParam>() {
                        
                        @Override
                        public int compare(InParam o1, InParam o2) {
                            return o1.getIndice().compareTo(o2.getIndice());
                        }
                    });
                    
                    for (InParam par : com.getPosiblesParams()) {
                        System.out.println();
                        System.out.println("\tparametro " + par.getIndice().toString() + ", tipo = " + par.getType().getDesc() + ", largo=" + par.getLargo().toString() + " \"" + par.getDescripcion() + "\"");
                    }
                    for (OutParam out : com.getPosiblesSalidas()) {
                        System.out.println();
                        System.out.println("\tsalida " + out.getCod().toString() + ", tipo = " + out.getType().getDesc() + ", largo=" + out.getLargo().toString() + " \"" + out.getDescripcion() + "\"");
                    }
                    
                    System.out.println();
                } else {
                    System.out.println(com.getNombreAbreviado() + " - " + com.getDescripcionComando());
                }
                
            }
        } else {
            System.out.println("no se encuentra la ayuda para " + commandName);
        }
        
    }
    
    public static Parameters getParameters() {
        return parameters;
    }
    
    static PortConfig getPortConfig() throws IOException {
        
        return new PortConfig((String) Pub.getParameters().getParamValue("ComPort"),
                (Integer) Pub.getParameters().getParamValue("BaudRate"),
                (Integer) Pub.getParameters().getParamValue("DataBits"),
                (Integer) Pub.getParameters().getParamValue("StopBits"),
                (Integer) Pub.getParameters().getParamValue("Parity"));
        
    }
    
    public static void addOptions() {
        
        parameters.addParam(Arrays.asList("WithNroRef", "withnroref"), Boolean.class, "compatibilidad con PFBatch para aceptar el 1er parametro nro de referencia de tiquet. Si no se coloca este modificador no se admitirá este nro", true);
        parameters.addParam(Arrays.asList("OutExt", "outext"), String.class, "extension que se le agregará al archivo de salida. El valor por defecto es \".out\"");
        parameters.addParam(Arrays.asList("OutFileName", "outfilename", "O", "o"), String.class, "nombre del archivo de salida sin la extension. El valor por defecto es el mismo que el del archivo de entrada");
        parameters.addParam(Arrays.asList("InFileName", "infilename", "i", "I"), String.class, "nombre del archivo de entrada.");
        parameters.addParam(Arrays.asList("Help", "help"), Boolean.class, "muestra esta ayuda", true);
        parameters.addParam(Arrays.asList("ListOptions", "listoptions"), Boolean.class, "lista las opciones del archivo de configuracion", true);
        parameters.addParam(Arrays.asList("ChangeLog", "changelog"), Boolean.class, "muestra changelog", true);
        parameters.addParam(Arrays.asList("Debug", "debug"), Boolean.class, "muestra esta ayuda", true);
        parameters.addParam(Arrays.asList("AsServer", "asserver"), Boolean.class, "Ejecuta el programa en modo servidor escuchando en el puerto seteado con el parametro TCPPort", true);
        parameters.addParam(Arrays.asList("AsClient", "asclient"), Boolean.class, "Ejecuta el programa en modo cliente hablando en el puerto seteado con el parametro TCPPort", true);
        parameters.addParam(Arrays.asList("HostName", "hostname"), String.class, "Especifica el nombre o la ip del servidor");
        parameters.addParam(Arrays.asList("TCPPort", "tcpport"), Integer.class, "Especifica puerto tcp en que va a hablar o escuchar");
        parameters.addParam(Arrays.asList("Version", "version", "v"), Boolean.class, "muestra la version", true);
        parameters.addParam(Arrays.asList("TestMode", "testmode"), Boolean.class, "modo de testeo, envia la información a la pantalla y devuelve un texto de prueba", true);
        parameters.addParam(Arrays.asList("HelpTipos", "helptipos"), Boolean.class, "muestra ayuda sobre los tipos de datos", true);
        parameters.addParam(Arrays.asList("HelpOut", "helpout"), Boolean.class, "muestra ayuda sobre la salida de los comandos", true);
        parameters.addParam(Arrays.asList("BaudRate", "baudrate"), Integer.class, "baudios de conexión, por defecto 9600");
        parameters.addParam(Arrays.asList("CopyInFile", "copyinfile", "cif"), Boolean.class, "Copia el archivo de entrada");
        parameters.addParam(Arrays.asList("TimeOut", "timeout"), Integer.class, "Tiempo de espera en milisegundos");
        parameters.addParam(Arrays.asList("Retardo", "retardo"), Integer.class, "Tiempo de retardo entre ejecuciones en milisegundos");
        parameters.addParam(Arrays.asList("ComPort", "comport"), String.class, "puerto de conexión, por defecto /dev/ttyS0");
        parameters.addParam(Arrays.asList("DataBits", "databits"), Integer.class, "bit de datos con que se configura al puerto serie");
        parameters.addParam(Arrays.asList("StopBits", "stopbits"), Integer.class, "bit de parada con que se configura al puerto serie, pueden ser 1, 2 o 3 si es 1.5");
        parameters.addParam(Arrays.asList("Parity", "parity"), Integer.class, "tipo de paridad con que se configura al puerto serie, puede ser Even = 2, Mark = 3, None = 0, Odd = 1, Space = 4, por defecto es 0 (None)");
        parameters.addParam(Arrays.asList("Sep", "sep"), String.class, "caracter utilizado como separador en los archivos de entrada");
        parameters.addParam(Arrays.asList("HelpCommand", "helpcommand"), String.class, "muestra la ayuda de comando, si se le agrega el nombre del comando solamente mostrará la ayuda de este", true);
        parameters.addParam(Arrays.asList("ListCommands", "listcommands"), String.class, "Lista los comandos", true);
    }
    
    public static void listOptions() {
        IniParametersFormatter ini = new IniParametersFormatter();
        
        ini.getConfig().put("addEqual", Boolean.TRUE);
        
        try {
            System.err.println(ini.getDocument(parameters));
        } catch (Exception ex) {
            Logger.getLogger(Pub.class).log(Level.ERROR, "Error al obtener el listado de opciones cargadas", ex);
        }
        
    }
    
    private static Properties getVersionProperties() {
        InputStream is = null;
        
        Properties tmp = new Properties();
        
        try {
            is = Main.class.getResourceAsStream("/version.properties");
            
            tmp.load(is);
        } catch (IOException ex) {
            Logger.getLogger(Pub.class).log(Level.ERROR, "Error al obtener minor de la aplicacion en version.properties", ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(Pub.class).log(Level.ERROR, "No se pudo cerrar el stream de entrada", ex);
                }
            }
        }
        
        return tmp;
    }

    /**
     * Busca dentro del archivo version.properties la version de la app
     *
     * @return
     */
    public static String getVersion() {
        
        String build;
        String major;
        String minor;
        
        InputStream is = null;
        
        Properties tmp = Pub.getVersionProperties();
        
        build = tmp.getProperty("BUILD", "0");
        major = tmp.getProperty("MAJOR", "0");
        minor = tmp.getProperty("MINOR", "0");
        
        return major + "." + minor + "_build_" + build;
    }
    
    public static String getChangeLog() {
        
        try (InputStream is = Main.class.getResourceAsStream("/changelog.txt")) {
            
            return readStream(is);
            
        } catch (IOException ex) {
            Logger.getLogger(Pub.class).log(Level.ERROR, "Error al obtener changelog.txt", ex);
        }        
        
        return "";
    }
    
    private static String readStream(InputStream iStream) throws IOException {
        StringBuilder builder;
        //build a buffered Reader, so that i can read whole line at once
        //build a Stream Reader, it can read char by char
        //build a buffered Reader, so that i can read whole line at once
        try (InputStreamReader iStreamReader = new InputStreamReader(iStream);
                BufferedReader bReader = new BufferedReader(iStreamReader)) {
            String line = null;
            builder = new StringBuilder();
            while ((line = bReader.readLine()) != null) {  //Read till end
                builder.append(line);
                builder.append("\n");
            }
        }
        
        return builder.toString();
    }
    
}
