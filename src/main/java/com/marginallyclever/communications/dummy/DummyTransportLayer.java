package com.marginallyclever.communications.dummy;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Instanciate a Dummy Connection for testing purpose.
 * Always reply ok to sent messages
 */
public class DummyTransportLayer implements TransportLayer {
    private static final Logger logger = LoggerFactory.getLogger(DummyTransportLayer.class);
    @Override
    public NetworkSession openConnection(Configuration configuration) {
        return new NetworkSession() {
            @Override
            public void closeConnection() {
                logger.debug("Closed");
            }

            @Override
            public void openConnection(String connectionName) throws Exception {
                logger.debug("Open");
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
