package com.marginallyclever.communications;

import org.junit.jupiter.api.Test;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.log.Log;

public class SerialTransportLayerTest {
	
	@Test
	public void scanConnections() {
		SerialTransportLayer layer = new SerialTransportLayer();
				
		String [] connectionNames = SerialTransportLayer.listConnections();
		if(connectionNames.length<=0) {
			Log.message("No serial connections found.");
			return;
		}
		
		NetworkSession [] connections = new NetworkSession[connectionNames.length];
		for(int i=0;i<connectionNames.length;++i) {
			connections[i] = layer.openConnection(connectionNames[i]);
			System.out.println("Found SerialTransportLayer "+connections[i].getName());
			connections[i].closeConnection();
		}
	}
}
