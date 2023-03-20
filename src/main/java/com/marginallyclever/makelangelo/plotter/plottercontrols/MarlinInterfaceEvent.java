package com.marginallyclever.makelangelo.plotter.plottercontrols;

/**
 * MarlinInterface sends this to let listeners know about important events.
 */
public class MarlinInterfaceEvent {
    /**
     * MarlinInterface sends this to let listeners know it is ready to receive more commands.
     */
    public static final int IDLE = 1;
    /**
     * MarlinInterface sends this to let listeners know a generic error occurred.
     */
    public static final int ERROR = 2;
    /**
     * MarlinInterface sends this to let listeners know the robot demands to home first.
     */
    public static final int HOME_XY_FIRST = 3;
    /**
     * MarlinInterface sends this to let listeners know there is an error in the transmission.
     * MarlinInterface contains a short history of commands that have been sent in case one needs to be resent.
     * If the requested command is no longer in the history this error is sent.
     */
    public static final int DID_NOT_FIND = 4;
    /**
     * MarlinInterface sends this to let listeners know there is an action command.
     * Action commands are used by the robot to request feedback from the user.
     * Typical use for a plotter is a color change command.
     */
    public static final int ACTION_COMMAND = 5;

    /**
     * MarlinInterface sends this to let listeners know there is no news from the robot.
     * There should have been news and very likely the connection has failed.
     */
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
