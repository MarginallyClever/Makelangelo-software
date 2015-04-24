package com.marginallyclever.communications;

import jssc.SerialPort;
import jssc.SerialPortEventListener;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public interface MarginallyCleverSerialPortEventListener extends SerialPortEventListener {

    boolean isPortConfirmed();

    SerialPort getSerialPort();

    String[] ListSerialPorts();

    void ClosePort();

    String[] getPortsDetected();

    int OpenPort(String portName);

    boolean isPortOpened();

    void reconnectToLastPort();

    String getRecentPort();
}
