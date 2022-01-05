package com.marginallyclever.communications;

import java.util.EventObject;

public class NetworkSessionEvent extends EventObject {
    /**
     *
     */
    private static final long serialVersionUID = -1727188492088759549L;
    // connection has been opened
    public static final int CONNECTION_OPENED = 1;
    // connection has closed
    public static final int CONNECTION_CLOSED = 2;
    // something has arrived.  Data is (String)data.
    public static final int DATA_RECEIVED = 4;
    // something has just been sent.  Data is (String)data.
    public static final int DATA_SENT = 5;

    public int flag;
    public Object data;

    public NetworkSessionEvent(Object source, int flag, Object data) {
        super(source);
        this.flag = flag;
        this.data = data;
    }
}
