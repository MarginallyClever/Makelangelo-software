package com.marginallyclever.communications;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import jssc.SerialPortList;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class SerialTransportLayerTest {
    @Test
    public void scanConnectionsNix() {

        String osName = System.getProperty("os.name");

        try (MockedStatic<SerialPortList> serialPortListMocked = Mockito.mockStatic(SerialPortList.class)) {
            System.setProperty("os.name", "mac");

            serialPortListMocked.when(() -> SerialPortList.getPortNames(any(Pattern.class))).thenAnswer(invocation ->
                    Stream.of("/dev/cu.Bluetooth-Incoming-Port", "/dev/cu.SRS-XB33", "/dev/cu.usbserial-1444140", "/dev/cu.robot-SerialPort", "/dev/cu.usbserial-1410", "/dev/ttyt3", "/dev/ttys001", "/dev/tty.1")
                            .filter(filename -> ((Pattern) invocation.getArgument(0)).matcher(filename).find())
                            .toArray(String[]::new)
            );
            List<String> connectionNames = new SerialTransportLayer().listConnections();
            assertEquals(List.of("/dev/cu.usbserial-1410", "/dev/cu.usbserial-1444140", "/dev/cu.Bluetooth-Incoming-Port", "/dev/cu.SRS-XB33", "/dev/cu.robot-SerialPort", "/dev/tty.1", "/dev/ttys001"), connectionNames);

            // alternate order
            serialPortListMocked.when(() -> SerialPortList.getPortNames(any(Pattern.class))).thenAnswer(invocation ->
                    Stream.of("/dev/cu.usbserial-1444140", "/dev/tty.1", "/dev/cu.Bluetooth-Incoming-Port", "/dev/ttyt3", "/dev/ttys001", "/dev/cu.SRS-XB33", "/dev/cu.usbserial-1410")
                            .filter(filename -> ((Pattern) invocation.getArgument(0)).matcher(filename).find())
                            .toArray(String[]::new)
            );
            connectionNames = new SerialTransportLayer().listConnections();
            assertEquals(List.of("/dev/cu.usbserial-1410", "/dev/cu.usbserial-1444140", "/dev/cu.Bluetooth-Incoming-Port", "/dev/cu.SRS-XB33", "/dev/tty.1", "/dev/ttys001"), connectionNames);
        } finally {
            System.setProperty("os.name", osName);
        }
    }

    @Test
    public void scanConnectionsOtherOS() {

        String osName = System.getProperty("os.name");

        try (MockedStatic<SerialPortList> serialPortListMocked = Mockito.mockStatic(SerialPortList.class)) {
            System.setProperty("os.name", "windows");
            serialPortListMocked.when(SerialPortList::getPortNames).thenReturn(new String[]{"COM1", "COM4"});
            List<String> connectionNames = new SerialTransportLayer().listConnections();
            assertEquals(List.of("COM1", "COM4"), connectionNames);
        } finally {
            System.setProperty("os.name", osName);
        }
    }
}

