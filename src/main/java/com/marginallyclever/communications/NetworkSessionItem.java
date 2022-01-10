package com.marginallyclever.communications;

public class NetworkSessionItem {

    private final TransportLayer transportLayer;
    private final String connectionName;

    public NetworkSessionItem(TransportLayer transportLayer, String connectionName) {
        this.transportLayer = transportLayer;
        this.connectionName = connectionName;
    }

    public TransportLayer getTransportLayer() {
        return transportLayer;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String toString() {
        return connectionName;
    }
}
