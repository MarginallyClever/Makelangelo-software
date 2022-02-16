/*
 * Based on StreamGobbler.java from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html (year 2012 ?).
 * Altered by PPAC37 ( reading char / char and various functions for events via method override )
 */
package com.marginallyclever.makelangelo.firmwareUploader;

import java.io.*;
import java.util.Date;

/**
 * StreamGobbler (Threaded), to take care of an "output" (for the command point
 * of view so for us an "input") stream of a process running.
 */
public class ProcessExecStreamGobbler extends Thread {

	private InputStream is;
	private String type;
	private OutputStream os;
	//
	boolean haveFinish = false;
	boolean timeTag = true;
	//
	long readCount = 0;
	int lineCount = 0;
	StringBuilder stringBuilder = null;
	//
	// TODO : to implement : to identify frozen commands. (no output timer ...)
	private long startedAt;
	private long lastEventAt;

	public String getInBuffer() {
		return stringBuilder.toString();
	}

	public ProcessExecStreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	public ProcessExecStreamGobbler(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
		stringBuilder = new StringBuilder();
	}

	@Override
	public void run() {
		startedAt = System.currentTimeMillis();
		fireUpdateRunBegin();
		try {
			PrintWriter pw = null;
			if (os != null) {
				pw = new PrintWriter(os);
			}

			InputStreamReader isr = new InputStreamReader(is);

			int intRead;
			while ((intRead = isr.read()) != -1) {
				lastEventAt = System.currentTimeMillis();
				readCount++;
				// For the case of a redirection
				if (pw != null) {
					pw.write(intRead);
				}
				//
				Date dNow = new Date();
				// for line by lines events
				if (intRead != '\n') {
					stringBuilder.appendCodePoint(intRead);
				} else {
					eventAndFlushSB(dNow);
				}
				// for char by char events
				readEvent(dNow, intRead);
			}

			if (pw != null) {
				pw.flush();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// For the case of a final line with no line return.
		if (stringBuilder.length() != 0) {
			eventAndFlushSB(new Date());
		}
		fireUpdateRunEnd();
		haveFinish = true;
		doFinish();
	}

	/**
	 * To force a carriage return (in the case of an output flow interlaced with
	 * inputs ...
	 */
	public void inputCarryReturn() {
		eventAndFlushSB(new Date());
	}

	/**
	 *
	 * @param dNow
	 */
	private void eventAndFlushSB(Date dNow) {
		String lineTmp = stringBuilder.toString();
		//
		lineCount++;
		readLineEventWithCounter(dNow, lineCount, lineTmp);
		//
		readLineEvent(dNow, lineTmp);
		// on vide le buffer de la ligne
		stringBuilder.setLength(0);
	}

	/**
	 * To Override to have read byte events. Be prepared to take care of non
	 * printable chars (used for exemple for some progress status to refresh the
	 * progress)
	 *
	 * @param d the time of the event
	 * @param intread the int value read from the stream.
	 */
	public void readEvent(Date d, int intread) {
		System.out.println("@todo StreamGobbler[" + type + "].readEvent( '" + (char) intread + "' = " + intread + " )");
	}

	/**
	 * To Override to have read lines events.
	 *
	 * @param d the time of the event.
	 * @param s the line readed from the stream.
	 */
	public void readLineEvent(Date d, String s) {
		System.out.println("@todo StreamGobbler[" + type + "].readLineEvent( " + s + " )");
	}

	/**
	 * To Override to have read lines events (with a line number).
	 *
	 * @param d the time of the event.
	 * @param lineNum the line number in the stream.
	 * @param s the line readed from the stream.
	 */
	public void readLineEventWithCounter(Date d, int lineNum, String s) {
		System.out.println("@todo StreamGobbler[" + type + "].readLineEventWithCounter( " + s + " )");
	}

	public void doFinish() {
		haveFinish = true;
	}

	//
	//
	//
	/**
	 * To Override to have a possible start event.
	 */
	public void fireUpdateRunBegin() {
	}

	/**
	 * To Override to have a possible end event.
	 */
	public void fireUpdateRunEnd() {
	}
}
