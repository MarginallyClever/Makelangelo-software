package com.marginallyclever.makelangelo.plotter.plottercontrols;

public class MarlinInterfaceEvent {
    // MarlinInterface sends this to let listeners know it can handle more input.
    public static final int IDLE = 1;
    // MarlinInterface sends this to let listeners know an error occurred.
    public static final int ERROR = 2;
    // MarlinInterface sends this to let listeners know it must home first.
    public static final int HOME_XY_FIRST = 3;
    // MarlinInterface sends this to let listeners know there is an error in the transmission.
    public static final int DID_NOT_FIND = 4;
    // MarlinInterface sends this to let listeners know there is an action command.
    public static final int ACTION_COMMAND = 5;

    // No news from the robot
    public static final int COMMUNICATION_FAILURE = 6;

    private final Object source;
    private final int id;
    private final String command;

    public MarlinInterfaceEvent(Object source, int id, String command) {
        this.source = source;
        this.id = id;
        this.command = command;
    }

    public String getActionCommand() {
        return command;
    }

    public int getID() {
        return id;
    }

    public Object getSource() {
        return source;
    }
}
