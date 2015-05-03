package com.marginallyclever.communications;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public interface MarginallyCleverConnection {
    String[] ListSerialPorts();
    String[] getPortsDetected();


    void closeConnection();
    int openConnection(String portName);

    void reconnectToLastPort();

    boolean isPortOpened();
    boolean isPortConfirmed();
    String getRecentPort();
    
    public void sendMessage(String msg) throws Exception;
}
