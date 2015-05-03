package com.marginallyclever.communications;

import java.util.prefs.Preferences;

import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import jssc.*;

/**
 * Lists available serial connections and opens a connection of that type
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialConnectionManager implements MarginallyCleverConnectionManager {
    private String[] portsDetected;
    private String recentPort;

	private MainGUI mainGUI;
	private MultilingualSupport translator;
	private MachineConfiguration machine;
	private Preferences prefs;

	
    public SerialConnectionManager(Preferences prefs, MainGUI mainGUI, MultilingualSupport translator, MachineConfiguration machine) {
        this.mainGUI = mainGUI;
        this.translator = translator;
        this.machine = machine;
        this.prefs = prefs;
        loadRecentPortFromPreferences(); //FIXME smelly
    }

    /**
     * find all available serial ports
     * @return a list of port names
     */
    @Override
    public String[] listConnections() {
        String OS = System.getProperty("os.name").toLowerCase();

        if(OS.indexOf("mac") >= 0){
            portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("OS X");
        } else if(OS.indexOf("win") >= 0) {
            portsDetected = SerialPortList.getPortNames("COM");
            //System.out.println("Windows");
        } else if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0){
            portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("Linux/Unix");
        } else {
            System.out.println("OS ERROR");
            System.out.println("OS NAME="+System.getProperty("os.name"));
        }
        return portsDetected;
    }
    
    /**
     * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
     */
    public MarginallyCleverConnection openConnection(String connectionName) {
    	 if(connectionName.equals(recentPort)) return null;
    	 
    	 SerialConnection serialConnection = new SerialConnection(mainGUI, translator, machine);

         mainGUI.Log("<font color='green'>" + translator.get("ConnectingTo") + connectionName + "...</font>\n");

         try {
        	 serialConnection.openConnection(connectionName);
         }
         catch(Exception e) {
	         mainGUI.Log("<span style='color:red'>" + translator.get("PortOpened") + "</span>\n");
    		 return null;
         }
         
         mainGUI.Log("<span style='color:green'>" + translator.get("PortOpened") + "</span>\n");
         mainGUI.updateMenuBar();
         mainGUI.PlayConnectSound();
    	 return serialConnection;
    }
    

    // pull the last connected port from prefs
    private void loadRecentPortFromPreferences() {
        recentPort = prefs.get("recent-port", "");
    }

    // update the prefs with the last port connected and refreshes the menus.
    // TODO: only update when the port is confirmed?
    public void SetRecentPort(String portName) {
        prefs.put("recent-port", portName);
        recentPort=portName;
        //UpdateMenuBar(); FIXME
    }


    /**
     *
     * @return the most recent port used by this serial connection.
     */
    @Override
    public String getRecentConnection() {
        return recentPort;
    }
}
