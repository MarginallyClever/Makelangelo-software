package com.marginallyClever.communications;

import java.util.List;

/**
 * Lists available connections of a type (serial, ethernet, smoke signals, etc) and opens a connection of that type
 * See also "OSI Layers" (Google it)
 * @author Dan
 * @since v7.1.0.0
 */
public interface TransportLayer {
  
    /**
     * opens a connection
     * @param configuration where to connect
     * @return a connection to the device at address <code>connectionName</code>
     */
    NetworkSession openConnection(Configuration configuration);

    /**
     * List availables connections
     * @return availables connections
     */
    List<String> listConnections();
}
