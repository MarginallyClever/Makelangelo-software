package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSessionEvent;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;

/**
 * {@link MarlinPlotterPanel} is a {@link MarlinPanel} with extra
 * instructions for interaction with a plotter robot.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class MarlinPlotterPanel extends MarlinPanel {
	private static final Logger logger = LoggerFactory.getLogger(MarlinPlotterPanel.class);

	private static final String STR_FEEDRATE = "echo:  M203";
	private static final String STR_ACCELERATION = "echo:  M201";
	private static final String MOTOR_ENGAGE = "M17";
	private static final String MOTOR_DISENGAGE = "M18";

	// M665 Set delta/polargraph configuration.
	private static final String M665 = "M665";

	private final Plotter myPlotter;

	public MarlinPlotterPanel(Plotter plotter, ChooseConnection chooseConnection) {
		super(chooseConnection);

		myPlotter = plotter;

		plotter.addPlotterEventListener(this::onPlotterEvent);
	}

	public void stopListeningToPlotter() {
		myPlotter.removePlotterEventListener(this::onPlotterEvent);
	}

	private void onPlotterEvent(PlotterEvent e) {
		switch (e.type) {
			case PlotterEvent.HOME_FOUND ->
				// logger.debug("MarlinPlotterInterface heard plotter home.");
					sendFindHome();
			case PlotterEvent.POSITION ->
				// logger.debug("MarlinPlotterInterface heard plotter move.");
					sendGoto();
			case PlotterEvent.PEN_UPDOWN ->
				// logger.debug("MarlinPlotterInterface heard plotter up/down.");
					sendPenUpDown();
			case PlotterEvent.MOTORS_ENGAGED ->
				// logger.debug("MarlinPlotterInterface heard plotter engage.");
					sendEngage();
			case PlotterEvent.TOOL_CHANGE ->
				// logger.debug("MarlinPlotterInterface heard plotter tool change.");
					sendToolChange((int) e.extra);
			default -> {
			}
		}
	}

	private void sendToolChange(int toolNumber) {
		queueAndSendCommand(myPlotter.getSettings().getPenUpString());
		queueAndSendCommand(myPlotter.getSettings().getToolChangeString(toolNumber));
	}

	private void sendFindHome() {
		queueAndSendCommand(myPlotter.getSettings().getFindHomeString());
	}

	private void sendPenUpDown() {
		String str = myPlotter.getPenIsUp()
				? myPlotter.getSettings().getPenUpString()
				: myPlotter.getSettings().getPenDownString();
		queueAndSendCommand(str);
	}

	private void sendEngage() {
		queueAndSendCommand(myPlotter.getMotorsEngaged() ? MOTOR_ENGAGE : MOTOR_DISENGAGE);
	}

	private void sendGoto() {
		Point2d p = myPlotter.getPos();
		String msg = myPlotter.getPenIsUp()
				? myPlotter.getSettings().getTravelToString(p.x, p.y)
				: myPlotter.getSettings().getDrawToString(p.x, p.y);
		queueAndSendCommand(msg);
	}

	/**
	 * M665: Set POLARGRAPH plottersettings
	 * Parameters:
	 *   S[segments]  - Segments-per-second
	 *   L[left]      - Work area minimum X
	 *   R[right]     - Work area maximum X
	 *   T[top]       - Work area maximum Y
	 *   B[bottom]    - Work area minimum Y
	 *   H[length]    - Maximum belt length
	 */
	private void sendSizeUpdate() {
		var settings = myPlotter.getSettings();
		var top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		var bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		var left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		var right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		var width = right-left;
		var height = top-bottom;
		var maxLen = Math.sqrt(width*width + height*height);

		queueAndSendCommand(M665
				+" T"+StringHelper.formatDouble(top)
				+" B"+StringHelper.formatDouble(bottom)
				+" L"+StringHelper.formatDouble(left)
				+" R"+StringHelper.formatDouble(right)
				+" S"+ settings.getInteger(PlotterSettings.SEGMENTS_PER_SECOND)
				+" H"+StringHelper.formatDouble(maxLen));
	}

	/**
	 * This does not fire on the Swing EVT thread.  Be careful!  Concurrency problems may happen.
	 * @param evt the network session event
	 */
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
			Point2d pos = myPlotter.getPos();

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
			myPlotter.getSettings().setDouble(PlotterSettings.MAX_ACCELERATION,v);
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
			myPlotter.getSettings().setDouble(PlotterSettings.FEED_RATE_DRAW,v);
		} catch (Exception e) {
			logger.warn("M203 problem, continuing anyway: {}", message);
		}
	}
}
