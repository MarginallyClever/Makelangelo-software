package com.marginallyclever.communications;

public class NetworkSessionItem {

    private final TransportLayer transportLayer;
    private final Configuration connectionConfiguration;

    public NetworkSessionItem(TransportLayer transportLayer, Configuration connectionConfiguration) {
        this.transportLayer = transportLayer;
        this.connectionConfiguration = connectionConfiguration;
    }

    public TransportLayer getTransportLayer() {
        return transportLayer;
    }

    public Configuration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    public String toString() {
        return connectionConfiguration.getConnectionName();
    }
}
