package com.marginallyclever.communications.dummy;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;

import java.util.List;

/**
 * Instanciate a Dummy Connection for testing purpose.
 * Always reply ok to sent messages
 */
public class DummyTransportLayer implements TransportLayer {

    @Override
    public NetworkSession openConnection(Configuration configuration) {
        return new NetworkSession() {
            @Override
            public void closeConnection() {

            }

            @Override
            public void openConnection(String connectionName) throws Exception {

            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public void sendMessage(String msg) throws Exception {
                notifyDataReceived("ok"); // Marlin answer, lower case
            }
        };
    }

    @Override
    public List<String> listConnections() {
        return List.of("dummy");
    }
}
