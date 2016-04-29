package com.marginallyclever.communications;

/**
 * Lists available connections of a type (serial, ethernet, smoke signals, etc) and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public interface MarginallyCleverConnectionManager {
  /**
   * @return a list of addresses of a connection type at which robots are likely to exist.
   */
  public String[] listConnections();

  /**
   * opens a connection
   * @param connectionName where to connect
   * @return a connection to the device at address <code>connectionName</code>
   */
  public MarginallyCleverConnection openConnection(String connectionName);

  /**
   * @return the name of the most recently opened connection.
   */
  public String getRecentConnection();
}
