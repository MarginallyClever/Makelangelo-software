package com.marginallyclever.communications;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;


/**
 * Created on 4/12/15.  Encapsulate all jssc serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7
 */
public final class SerialConnection implements SerialPortEventListener, MarginallyCleverConnection {
  private SerialPort serialPort;
  private static final int BAUD_RATE = 57600;

  private String connectionName = "";
  private boolean portOpened = false;
  private boolean portConfirmed = false;

  private String robot_type_name = "DRAWBOT";  // FIXME doesn't belong in connection, should be a higher class
  private String hello = "HELLO WORLD! I AM " + robot_type_name + " #";  // FIXME doesn't belong in connection, should be a higher class

  static private String CUE = "> ";
  static private String NOCHECKSUM = "NOCHECKSUM ";
  static private String BADCHECKSUM = "BADCHECKSUM ";
  static private String BADLINENUM = "BADLINENUM ";

  // parsing input from Makelangelo
  private String serial_recv_buffer = "";
  // prevent repeating pings from appearing in console
  boolean lastLineWasCue = false;

  private final MainGUI mainGUI;
  private final MultilingualSupport translator;
  private final MakelangeloRobot machine;


  public SerialConnection(MainGUI mainGUI, MultilingualSupport translator, MakelangeloRobot machine) {
    this.mainGUI = mainGUI;
    this.translator = translator;
    this.machine = machine;
  }

  @Override
  public void sendMessage(String msg) throws Exception {
    try {
      serialPort.writeBytes(msg.getBytes());
    } catch (SerialPortException e) {
      throw new Exception(e.getMessage());
    }
  }


  @Override
  public void closeConnection() {
    if (portOpened) {
      if (serialPort != null) {
        try {
          serialPort.removeEventListener();
          serialPort.closePort();
        } catch (SerialPortException e) {
        }
      }
      portOpened = false;
      portConfirmed = false;
    }
  }

  // open a serial connection to a device.  We won't know it's the robot until
  @Override
  public void openConnection(String portName) throws Exception {
    if (portOpened) return;

    closeConnection();

    // open the port
    serialPort = new SerialPort(portName);
    serialPort.openPort();// Open serial port
    serialPort.setParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    serialPort.addEventListener(this);

    connectionName = portName;
    portOpened = true;
    lastLineWasCue = false;
  }


  /**
   * Check if the robot reports an error and if so what line number.
   *
   * @return -1 if there was no error, otherwise the line number containing the error.
   */
  protected int errorReported() {
    if (portConfirmed == false) return -1;

    if (serial_recv_buffer.lastIndexOf(NOCHECKSUM) != -1) {
      String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(NOCHECKSUM) + NOCHECKSUM.length());
      String x = getNumberPortion(after_error);
      int err = 0;
      try {
        err = Integer.decode(x);
      } catch (Exception e) {
      }

      return err;
    }
    if (serial_recv_buffer.lastIndexOf(BADCHECKSUM) != -1) {
      String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
      String x = getNumberPortion(after_error);
      int err = 0;
      try {
        err = Integer.decode(x);
      } catch (Exception e) {
      }

      return err;
    }
    if (serial_recv_buffer.lastIndexOf(BADLINENUM) != -1) {
      String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(BADLINENUM) + BADLINENUM.length());
      String x = getNumberPortion(after_error);
      int err = 0;
      try {
        err = Integer.decode(x);
      } catch (Exception e) {
      }

      return err;
    }

    return -1;
  }


  /**
   * Complete the handshake, load robot-specific configuration, update the menu, repaint the preview with the limits.
   *
   * @return true if handshake succeeds.
   */
  public boolean confirmPort() {
    if (portConfirmed == true) return true;
    if (serial_recv_buffer.lastIndexOf(hello) < 0) return false;

    portConfirmed = true;

    String after_hello = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(hello) + hello.length());
    machine.parseRobotUID(after_hello);

    mainGUI.getMainframe().setTitle(translator.get("TitlePrefix")
        + Long.toString(machine.getUID())
        + translator.get("TitlePostfix"));

    mainGUI.sendConfig();
    mainGUI.getDrawPanel().updateMachineConfig();

    mainGUI.updateMenuBar();
    mainGUI.getDrawPanel().setConnected(true);

    // rebuild the drive pane so that the feed rates are correct.
    mainGUI.updatedriveControls();

    return true;
  }


  // Deal with something robot has sent.
  @Override
  public void serialEvent(SerialPortEvent events) {
    if (events.isRXCHAR()) {
      try {
        int len = events.getEventValue();
        byte[] buffer = serialPort.readBytes(len);
        String line2 = new String(buffer, 0, len);

        serial_recv_buffer += line2;
        // wait for the cue ("> ") to send another command
        if (serial_recv_buffer.lastIndexOf(CUE) != -1) {
          String line2_mod = serial_recv_buffer;
          //line2_mod = line2.mod.replace("\n", "");
          //line2_mod = line2_mod.replace(">", "");
          line2_mod = line2_mod.trim();
          if (line2_mod.length() > 0) {
            if (line2_mod.equals(CUE.trim())) {
              if (lastLineWasCue == true) {
                // don't repeat the ping
                //Log("<span style='color:#FF00A5'>"+line2_mod+"</span>");
              } else {
                mainGUI.log("<span style='color:#FFA500'>" + line2_mod + "</span>");
              }
              lastLineWasCue = true;
            } else {
              lastLineWasCue = false;
              mainGUI.log("<span style='color:#FFA500'>" + line2_mod + "</span>");
            }
          }

          int error_line = errorReported();
          if (error_line != -1) {
            mainGUI.getGcodeFile().linesProcessed = error_line;
            serial_recv_buffer = "";
            mainGUI.sendFileCommand();
          } else if (confirmPort()) {
            serial_recv_buffer = "";
            mainGUI.sendFileCommand();
          }
        }
      } catch (SerialPortException e) {
      }
    }
  }


  // connect to the last port
  @Override
  public void reconnect() throws Exception {
    openConnection(connectionName);
  }

  /**
   * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
   *
   * @param src
   * @return the portion of the string that is actually a number
   */
  private String getNumberPortion(String src) {
    src = src.trim();
    int length = src.length();
    String result = "";
    for (int i = 0; i < length; i++) {
      Character character = src.charAt(i);
      if (Character.isDigit(character)) {
        result += character;
      }
    }
    return result;
  }


  /**
   * @return <code>true</code> if the serial port has been confirmed; <code>false</code> otherwise
   */
  @Override
  public boolean isRobotConfirmed() {
    return portConfirmed;
  }

  /**
   * @return the port open for this serial connection.
   */
  @Override
  public boolean isConnectionOpen() {
    return portOpened;
  }

  @Override
  public String getRecentConnection() {
    return connectionName;
  }
}
