package com.marginallyclever.communications;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class SerialTransportLayerTest {

    @Test
    public void scanConnections() {
        SerialTransportLayer layer = new SerialTransportLayer();

        String[] connectionNames = SerialTransportLayer.listConnections();
        if (connectionNames.length <= 0) {
            fail("No serial connections found.");
        }

        NetworkSession[] connections = new NetworkSession[connectionNames.length];
        for (int i = 0; i < connectionNames.length; ++i) {
            connections[i] = layer.openConnection(connectionNames[i]);
            assertNotNull(connections[i].getName());
            connections[i].closeConnection();
        }
    }
}
