package com.marginallyclever.communications;

import com.marginallyclever.communications.serial.SerialTransportLayer;

import java.util.Arrays;
import java.util.List;

public class TransportLayers {
    public static List<TransportLayer> transportLayers = Arrays.asList(
        new SerialTransportLayer()
        //new TCPTransportLayer()
	);

}
