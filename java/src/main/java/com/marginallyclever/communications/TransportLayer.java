package com.marginallyclever.communications;

import java.awt.Component;

/**
 * Lists available connections of a type (serial, ethernet, smoke signals, etc) and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public interface TransportLayer {
  /**
   * opens a connection
   * @param connectionName where to connect
   * @return a connection to the device at address <code>connectionName</code>
   */
  public NetworkConnection openConnection(String connectionName);

  /**
   * Create a new connection to a device.  This may include creating a gui to ask questions of the user
   * @return a connection to the device at address <code>connectionName</code>
   */
  public NetworkConnection requestNewConnection(Component parent);

}
