package com.marginallyclever.communications;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public interface MarginallyCleverConnection {
    String[] ListConnections();
    String[] getConnectionsDetected();


    void closeConnection();
    int openConnection(String portName);

    void reconnect();

    boolean isConnectionOpen();
    boolean isConnectionConfirmed();
    String getRecentConnection();
    
    public void sendMessage(String msg) throws Exception;
}
