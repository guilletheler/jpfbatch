package com.gt.jpfbatch;

import com.gt.ifepson.capa_fisica.DataType;
import com.gt.ifepson.capa_fisica.OutParam;
import com.gt.ifepson.capa_fisica.serialPort.PortConfig;
import com.gt.ifepson.fiscaldoc.FiscalDocument;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author guillermot
 */
public class Main {

    public static void main(String[] args) throws IOException, Exception {
        
        Pub.init(args);
        
        //jpfbatch.tests.IFTest.doTiquetTest();
        
        
        //System.exit(0);
                

        if (Pub.getParameters().isValueSetted("help")) {

            Logger.getLogger(Main.class).log(Level.DEBUG, "\n Ayuda de JPFBatch\n"
                    + "---------------------\n");

            Pub.getParameters().printHelpOn(System.out);
        } else if (Pub.getParameters().isValueSetted("ListOptions")) {
            Pub.listOptions();
        } else if (Pub.getParameters().isValueSetted("v")) {
            System.out.println(Pub.getVersion());
        } else if (Pub.getParameters().isValueSetted("changelog")) {
            System.out.println(Pub.getChangeLog());
        } else if (Pub.getParameters().isValueSetted("HelpCommand")) {
            if (Pub.getParameters().haveValue("HelpCommand")) {
                Pub.showHelp((String) Pub.getParameters().getParamValue("HelpCommand"));
            } else {
                // toda la ayuda
                List<String> keys = new ArrayList<>(FiscalDocument.getComandosDisponibles().keySet());
                String[] keys1 = keys.toArray(new String[]{});
                Arrays.sort(keys1);
                for (String s : keys1) {
                    Pub.showHelp(s);
                }
            }
        } else if (Pub.getParameters().isValueSetted("ListCommands")) {
            List<String> keys = new ArrayList<>(FiscalDocument.getComandosDisponibles().keySet());
            String[] keys1 = keys.toArray(new String[]{});
            Arrays.sort(keys1);
            System.out.println("Listado de comandos");
            for (String s : keys1) {
                Pub.showHelp(s, false);
            }
        } else if (Pub.getParameters().isValueSetted("HelpTipos")) {
            System.out.println("Ayuda de JPFBatch - tipos da datos\n"
                    + "-------------------------------------\n");

            for (DataType tf : DataType.values()) {
                System.out.println(tf.getType() + " = " + tf.getDesc());
            }
        } else if (Pub.getParameters().isValueSetted("HelpOut")) {

            Logger.getLogger(Main.class).log(Level.DEBUG, "\n Ayuda de JPFBatch - salidas indexadas\n"
                    + "----------------------------------------\n");

            for (OutParam io : OutParam.values()) {
                System.out.println(io.getCod().toString() + " = " + io.getDescripcion());
            }
        } else if (Pub.getParameters().isValueSetted("AsServer")) {

            Logger.getLogger(Main.class).log(Level.DEBUG, "Corriendo como servidor");

            com.gt.jpfbatch.net.TCPServer server = new com.gt.jpfbatch.net.TCPServer((Integer) Pub.getParameters().getParamValue("TimeOut"), Pub.getPortConfig());

            try {
                server.listen();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        } else if (Pub.getParameters().isValueSetted("AsClient")) {

            Logger.getLogger(Main.class).log(Level.DEBUG, "Corriendo como cliente");

            if (Pub.getParameters().isValueSetted("InFileName") && !((String) Pub.getParameters().getParamValue("InFileName")).isEmpty()) {

                com.gt.jpfbatch.net.TCPClient client = new com.gt.jpfbatch.net.TCPClient(loadFile((String) Pub.getParameters().getParamValue("InFileName")));

                try {
                    List<String> listaSalida = client.communicate(Pub.getParameters().isValueSetted("TestMode") || Pub.getParameters().isValueSetted("testmode"));

                    String outFileName = new File((String) Pub.getParameters().getParamValue("InFileName")).getName();

                    writeOut(outFileName, listaSalida);

                } catch (Exception ex) {
                    Logger.getLogger(Main.class).log(Level.FATAL, null, ex);
                }
            }
        } else if (Pub.getParameters().isValueSetted("InFileName") && !((String) Pub.getParameters().getParamValue("InFileName")).isEmpty()) {

            List<String> listaSalida = EjecutarBatchFromFile((String) Pub.getParameters().getParamValue("InFileName"), (Integer) Pub.getParameters().getParamValue("TimeOut"), Pub.getPortConfig());

            String outFileName = new File((String) Pub.getParameters().getParamValue("InFileName")).getName();

            writeOut(outFileName, listaSalida);

        }


        Logger.getLogger(Main.class).log(Level.DEBUG, "Todo terminado");

        System.exit(0);

    }

    public static String loadFile(String fileName) throws IOException {
        BufferedReader entrada = new BufferedReader(new FileReader(new File(fileName)));

        String retVal = "";
        String linea;

        while ((linea = entrada.readLine()) != null) {
            retVal += linea + "\n";
        }

        return retVal;
    }

    public static List<String> EjecutarBatchFromFile(String inFileName, int timeOut, PortConfig portConfig) throws FileNotFoundException, IOException {
        return EjecutarBatchFromFile(inFileName, timeOut, portConfig, false);
    }

    public static List<String> EjecutarBatchFromFile(String inFileName, int timeOut, PortConfig portConfig, boolean testMode) throws FileNotFoundException, IOException {

        if (Pub.getParameters().isValueSetted("CopyInFile") && !((String) Pub.getParameters().getParamValue("CopyInFile")).isEmpty()) {
            String copyFileName = (String) Pub.getParameters().getParamValue("CopyInFile");

            copyFileName = copyFileName.replace("%t%", Calendar.getInstance().getTimeInMillis() + "");

            Logger.getLogger(Main.class).log(Level.DEBUG, "Copiando archivo de entrada " + inFileName + " a " + copyFileName);
            
            File src = new File(inFileName);
            File dst = new File(copyFileName);
            
            Files.copy(src.toPath(), dst.toPath());
        }

        Logger.getLogger(Main.class).log(Level.DEBUG, "Generando batch de " + inFileName);

        Object o = Pub.getParameters().getParamValue("LargoDescItem");

        int largoItem = 16;

        if (o != null) {
            try {
                largoItem = (Integer) Pub.getParameters().getParamValue("LargoDescItem");
            } catch (Exception ex) {
                Logger.getLogger(Main.class).log(Level.DEBUG, "Generando batch de " + inFileName);
                largoItem = 16;
            }
        }

        FiscalDocument batch = FiscalDocument.fromFile(inFileName, timeOut, portConfig, (String) Pub.getParameters().getParamValue("Sep"), largoItem);

        return EjecutarBatch(batch, testMode);
    }

    public static List<String> EjecutarBatchFromString(String strBatch, int timeOut, PortConfig portConfig) throws FileNotFoundException, IOException {
        return EjecutarBatchFromString(strBatch, timeOut, portConfig, false);
    }

    public static List<String> EjecutarBatchFromString(String strBatch, int timeOut, PortConfig portConfig, boolean testMode) throws FileNotFoundException, IOException {

        Logger.getLogger(Main.class).log(Level.DEBUG, "Generando batch de String\n" + strBatch);

        int largoItem = 16;
        Object o = Pub.getParameters().getParamValue("LargoDescItem");
        if (o != null) {
            try {
                largoItem = (Integer) Pub.getParameters().getParamValue("LargoDescItem");
            } catch (Exception ex) {
                largoItem = 16;
            }
        }

        FiscalDocument batch = FiscalDocument.fromBatchString(strBatch, timeOut, portConfig, (String) Pub.getParameters().getParamValue("Sep"), largoItem);

        return EjecutarBatch(batch, testMode);
    }

    public static List<String> EjecutarBatch(FiscalDocument batch) {
        return EjecutarBatch(batch, Pub.getParameters().haveValue("TestMode"));
    }

    public static List<String> EjecutarBatch(FiscalDocument batch, boolean testMode) {

        List<String> retVal = null;

        //batch = new jpfbatch.tests.cierreZ();
        if (batch == null) {
            Logger.getLogger(Main.class).log(Level.DEBUG, "error al generar el batch");
        } else if (testMode) {
            // modo de prueba
            Logger.getLogger(Main.class).log(Level.DEBUG, "Modo de prueba");

            retVal = new ArrayList<>();

            System.out.println(batch.toString());

            retVal.add("Se envio la prueba");

        } else {

            retVal = new ArrayList<>();

            Logger.getLogger(Main.class).log(Level.DEBUG, "Ejecutando batch");

            batch.run();

            Logger.getLogger(Main.class).log(Level.DEBUG, "Fin ejecucion batch, preparando respuesta");

            List<OutParam> keys = new ArrayList<>(batch.getRespuesta().keySet());

            if (keys.size() > 0) {
                Collections.sort(keys, new Comparator<OutParam>() {

                    @Override
                    public int compare(OutParam o1, OutParam o2) {
                        return o1.getCod().compareTo(o2.getCod());
                    }
                });

                Integer largo;

                for (OutParam io : keys) {

                    largo = io.getLargo();

                    if (largo < 0) {
                        largo = batch.getRespuesta().get(io).length();
                    }
                    
                    Logger.getLogger(Main.class).log(Level.DEBUG, "Agregando a salida: " + StringUtils.leftPad(io.getCod().toString(), 4, '0') + io.getType().getType() + StringUtils.leftPad(largo.toString(), 2, '0') + batch.getRespuesta().get(io).toString() + " " + io.getDescripcion());

                    retVal.add(StringUtils.leftPad(io.getCod().toString(), 4, '0') + io.getType().getType() + StringUtils.leftPad(largo.toString(), 2, '0') + batch.getRespuesta().get(io).toString() + " " + io.getDescripcion());
                }
            }
        }

        return retVal;

    }

    public static void writeOut(String outFileName, List<String> listaSalida) throws IOException {

        Logger.getLogger(Main.class).log(Level.DEBUG, "Entrando a escribiendo salida");

        if (listaSalida.size() > 0) {

            if (outFileName.contains(".")) {
                outFileName = outFileName.substring(0, outFileName.lastIndexOf("."));
            }

            if (Pub.getParameters().isValueSetted("outfilename")) {
                outFileName = (String) Pub.getParameters().getParamValue("outfilename");
            }

            outFileName += Pub.getParameters().getParamValue("OutExt");

            Logger.getLogger(Main.class).log(Level.DEBUG, "Escribiendo salida " + outFileName);

            BufferedWriter salida = new BufferedWriter(new FileWriter(new File(outFileName)));

            Integer largo = 0;
            String logVal = "";

            for (String s : listaSalida) {
                salida.write(s + "\n");
            }

            salida.close();
        }

        Logger.getLogger(Main.class).log(Level.DEBUG, "Saliendo de escribiendo salida");

    }
}
