package com.marginallyclever.makelangelo.plotter.plottercontrols;

/**
 * {@link MarlinCommand} is used by {@link MarlinPanel} to buffer outgoing messages.
 * @author Dan Royer
 * @since 7.28.0
 */
public class MarlinCommand {
	// for quick retrieval
	public int lineNumber;
	// the complete command with line number and checksum
	public String command;

	public MarlinCommand(int number, String str) {
		lineNumber = number;
		command=str;
	}
}
