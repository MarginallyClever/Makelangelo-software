package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;

/**
 * {@link MarlinPlotterInterface} is a {@link MarlinInterface} with extra
 * instructions for interaction with a plotter robot.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class MarlinPlotterInterface extends MarlinInterface {
	@Serial
	private static final long serialVersionUID = -7114823910724405882L;

	private static final Logger logger = LoggerFactory.getLogger(MarlinPlotterInterface.class);

	private static final String STR_FEEDRATE = "echo:  M203";
	private static final String STR_ACCELERATION = "echo:  M201";

	private Plotter myPlotter;

	public MarlinPlotterInterface(Plotter plotter, ChooseConnection chooseConnection) {
		super(chooseConnection);

		myPlotter = plotter;

		plotter.addPlotterEventListener(this::onPlotterEvent);
	}

	public void stopListeningToPlotter() {
		myPlotter.removePlotterEventListener(this::onPlotterEvent);
	}

	private void onPlotterEvent(PlotterEvent e) {
		switch (e.type) {
		case PlotterEvent.HOME_FOUND:
			// logger.debug("MarlinPlotterInterface heard plotter home.");
			sendFindHome();
			break;
		case PlotterEvent.POSITION:
			// logger.debug("MarlinPlotterInterface heard plotter move.");
			sendGoto();
			break;
		case PlotterEvent.PEN_UPDOWN:
			// logger.debug("MarlinPlotterInterface heard plotter up/down.");
			sendPenUpDown();
			break;
		case PlotterEvent.MOTORS_ENGAGED:
			// logger.debug("MarlinPlotterInterface heard plotter engage.");
			sendEngage();
			break;
		case PlotterEvent.TOOL_CHANGE:
			// logger.debug("MarlinPlotterInterface heard plotter tool change.");
			sendToolChange((int) e.extra);
			break;
		default:
			break;
		}
	}

	private void sendToolChange(int toolNumber) {
		queueAndSendCommand(MarlinPlotterInterface.getPenUpString(myPlotter));
		queueAndSendCommand(getToolChangeString(toolNumber));
	}

	private void sendFindHome() {
		queueAndSendCommand("G28 XY");
	}

	private void sendPenUpDown() {
		String str = myPlotter.getPenIsUp() ? MarlinPlotterInterface.getPenUpString(myPlotter)
				: MarlinPlotterInterface.getPenDownString(myPlotter);
		queueAndSendCommand(str);
	}

	private void sendEngage() {
		queueAndSendCommand(myPlotter.getMotorsEngaged() ? "M17" : "M18");
	}

	private void sendGoto() {
		Point2D p = myPlotter.getPos();
		String msg = myPlotter.getPenIsUp() 
				? MarlinPlotterInterface.getTravelToString(myPlotter, p.x, p.y)
				: MarlinPlotterInterface.getDrawToString(myPlotter, p.x, p.y);
		queueAndSendCommand(msg);
	}

	@Override
	protected void onDataReceived(NetworkSessionEvent evt) {
		super.onDataReceived(evt);

		if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			String message = ((String)evt.data).trim();
			//logger.debug("MarlinPlotterInterface received '"+message.trim()+"'.");
			if (message.startsWith("X:") && message.contains("Count")) {
				onHearM114(message);
			} else if (message.startsWith(STR_FEEDRATE)) {
				onHearFeedrate(message);
			} else if (message.startsWith(STR_ACCELERATION)) {
				onHearAcceleration(message);
			}
		}
	}

	// format is normally X:0.00 Y:270.00 Z:0.00 Count X:0 Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the state data.
	protected void onHearM114(String message) {
		try {
			String position = message.substring(0, message.indexOf("Count"));
			String[] majorParts = position.split("\s");
			Point2D pos = myPlotter.getPos();

			for (String s : majorParts) {
				String[] minorParts = s.split(":");
				double v = Double.parseDouble(minorParts[1].trim());
				if (minorParts[0].equalsIgnoreCase("X")) pos.x = v;
				if (minorParts[0].equalsIgnoreCase("Y")) pos.y = v;
			}

			myPlotter.setPos(pos.x,pos.y);
		} catch (NumberFormatException e) {
			logger.warn("M114 problem, continuing anyway: {}", message);
		}
	}

	// format is "echo: M201 X5400.00 Y5400.00 Z5400.00"
	// I only care about the x value when reading.
	protected void onHearAcceleration(String message) {
		try {
			String position = message.substring(STR_ACCELERATION.length());
			String[] parts = position.split("\s");
			if (parts.length != 4)
				throw new Exception("M201 format bad: " + message);
			double v = Double.parseDouble(parts[1].substring(1));
			logger.debug("MarlinPlotterInterface found acceleration {}", v);
			myPlotter.getSettings().setAcceleration(v);
		} catch (Exception e) {
			logger.warn("M201 problem, continuing anyway: {}", message);
		}
	}

	// format is "echo: M203 X5400.00 Y5400.00 Z5400.00"
	// I only care about the x value when reading.
	protected void onHearFeedrate(String message) {
		try {
			String position = message.substring(STR_FEEDRATE.length());
			String[] parts = position.split("\s");
			if (parts.length != 4)
				throw new Exception("M203 format bad: " + message);
			double v = Double.parseDouble(parts[1].substring(1));
			logger.debug("MarlinPlotterInterface found feedrate {}", v);
			myPlotter.getSettings().setDrawFeedRate(v);
		} catch (Exception e) {
			logger.warn("M203 problem, continuing anyway: {}", message);
		}
	}

	// "By convention, most G-code generators use G0 for non-extrusion movements"
	// https://marlinfw.org/docs/gcode/G000-G001.html
	public static String getTravelToString(Plotter p,double x, double y) {
		return "G0 " + getPosition(x, y) + " F" + p.getSettings().getTravelFeedRate();
	}

	// "By convention, most G-code generators use G0 for non-extrusion movements"
	// https://marlinfw.org/docs/gcode/G000-G001.html
	public static String getDrawToString(Plotter p,double x, double y) {
		return "G1 " + getPosition(x, y) + " F" + p.getSettings().getDrawFeedRate();
	}

	private static String getPosition(double x, double y) {
		return "X" + StringHelper.formatDouble(x) + " Y" + StringHelper.formatDouble(y);
	}

	public static String getPenUpString(Plotter p) {
		return "M280 P0 S" + (int)p.getPenUpAngle() + " T" + (int) p.getPenLiftTime();
	}

	public static String getPenDownString(Plotter p) {
		return "M280 P0 S" + (int)p.getPenDownAngle() + " T50";
	}

	public static String getToolChangeString(int toolNumber) {
		String colorName = getColorName(toolNumber & 0xFFFFFF);
		return "M0 Ready " + colorName + " and click";
	}

	private static String getColorName(int toolNumber) {
		String name = switch (toolNumber) {
			case 0xff0000 -> "red";
			case 0x00ff00 -> "green";
			case 0x0000ff -> "blue";
			case 0x000000 -> "black";
			case 0x00ffff -> "cyan";
			case 0xff00ff -> "magenta";
			case 0xffff00 -> "yellow";
			case 0xffffff -> "white";
			default -> "0x" + Integer.toHexString(toolNumber); // display unknown RGB value as hex
		};
		return name;
	}
}
