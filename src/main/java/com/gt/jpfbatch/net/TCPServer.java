/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch.net;

import com.gt.ifepson.capa_fisica.serialPort.PortConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.gt.jpfbatch.Main;
import com.gt.jpfbatch.Pub;

/**
 *
 * @author guillermot
 */
public class TCPServer {

    List<clientThread> clientes = new ArrayList<clientThread>();
    int timeOut = 1000;
    PortConfig portConfig;

    public TCPServer(int timeOut, PortConfig portConfig) {
        this.timeOut = timeOut;
        this.portConfig = portConfig;
    }

    List<String> ejecutar(JPFBNetEventArgs evt) throws IOException {
        synchronized (TCPServer.class) {
            return Main.EjecutarBatchFromString(evt.getBatchString(), evt.getTimeOut(), evt.getPortConfig(), evt.isTestMode());
        }
    }

    public void listen() throws Exception {
        ServerSocket serverSocket = new ServerSocket((Integer) Pub.getParameters().getParamValue("TCPPort"));
        Socket clientSocket;

        while (true) {

            // un recolector de basura rudimentario...
            for (int i = clientes.size() - 1; i >= 0; i--) {
                if (!clientes.get(i).isAlive()) {
                    clientes.remove(i);
                }
            }

            clientSocket = serverSocket.accept();

            clientThread ct = new clientThread(clientSocket, "cliente" + clientes.size(), this.timeOut, this.portConfig);

            clientes.add(ct);

            ct.addEventListener(new JPFBNetEventListener() {

                @Override
                public List<String> onEjecutar(JPFBNetEventArgs evt) {
                    List<String> retVal = null;
                    try {
                        retVal = ejecutar(evt);
                    } catch (IOException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    return retVal;
                }
            });

            ct.start();

        }
    }
}
