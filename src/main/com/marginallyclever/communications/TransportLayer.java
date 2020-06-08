package com.marginallyclever.communications;

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
  public String getName();
  
  /**
   * opens a connection
   * @param connectionName where to connect
   * @return a connection to the device at address <code>connectionName</code>
   */
  public NetworkConnection openConnection(String connectionName);

  /**
   * @return a panel with the gui options for this transport layer
   */
  public TransportLayerPanel getTransportLayerPanel();
}
