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
		
		for(int i=0;i<connectionNames.length;++i) {
			NetworkSession c = layer.openConnection(connectionNames[i]);
			if(c!=null) {
				System.out.println("Found SerialTransportLayer "+c.getName());
				c.closeConnection();
			}
		}
	}
}
