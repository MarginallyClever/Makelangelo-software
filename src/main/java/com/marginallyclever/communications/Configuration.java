package com.marginallyclever.communications;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of a connection: transport layer, target, optionnal params
 */
public class Configuration {

    private TransportLayer transportLayer;
    private String connectionName;
    private Map<String, Object> configurations = new HashMap<>();

    public Configuration(TransportLayer transportLayer, String connectionName) {
        this.transportLayer = transportLayer;
        this.connectionName = connectionName;
    }

    public Map<String, Object> getConfigurations() {
        return configurations;
    }

    public void addConfiguration(String settings, Object value) {
        configurations.put(settings, value);
    }

    public String getConnectionName() {
        return connectionName;
    }

    public TransportLayer getTransportLayer() {
        return transportLayer;
    }

    public String toString() {
        return connectionName;
    }

}
