package com.marginallyclever.communications;

import java.util.List;

/**
 * Lists available connections of a type (serial, ethernet, smoke signals, etc) and opens a connection of that type
 * See also "OSI Layers" (Google it)
 * @author Dan
 * @since v7.1.0.0
 */
public interface TransportLayer {
	/**
	 * 
	 * @return english name of this transport layer
	 */
    String getName();
  
    /**
     * opens a connection
     * @param connectionName where to connect
     * @return a connection to the device at address <code>connectionName</code>
     */
    NetworkSession openConnection(String connectionName);

    List<String> listConnections();
}
