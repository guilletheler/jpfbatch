/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch.net;

import com.gt.ifepson.capa_fisica.serialPort.PortConfig;
import java.util.EventObject;

/**
 *
 * @author guillermot
 */
// Declare the event. It must extend EventObject.
public class JPFBNetEventArgs extends EventObject {
    
    String batch = "";
    
    int timeOut = 1000;
    
    PortConfig portConfig;
    
    boolean testMode = false;

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
    
    public PortConfig getPortConfig() {
        return portConfig;
    }

    public void setPortConfig(PortConfig portConfig) {
        this.portConfig = portConfig;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
    
    public String getBatchString() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public JPFBNetEventArgs(Object source, String batch, int timeOut, PortConfig portConfig, boolean testMode) {
        super(source);
        this.batch = batch;
        this.timeOut = timeOut;
        this.portConfig = portConfig;
        this.testMode = testMode;
    }

    public JPFBNetEventArgs(Object source) {
        super(source);
    }
}
