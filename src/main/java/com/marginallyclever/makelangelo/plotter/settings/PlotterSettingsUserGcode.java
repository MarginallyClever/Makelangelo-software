/*
 */
package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.util.FunctionSolver;
import com.marginallyclever.util.PreferencesHelper;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PlotterSettingsUserGcode {

	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsUserGcode.class);
	
	private String userGeneralStartGcode = "G28"; // TODO Should containe a G28 or a G28 is added befor any move...
	private String userGeneralEndGcode = "M300"; // TODO Should containe a disble motors or added at the end //  I think I just parked it 10cm from the left edge and 15cm from the top edge.
	private String userToolChangeStartGcode = "M300"; // Should go in relative moves befor and in absolut moves after ??? a save position ?
	private String userToolChangeEndGcode = ""; // a resort position ?

	public void setUserGeneralStartGcode(String userGeneralStartGcode) {
		this.userGeneralStartGcode = userGeneralStartGcode;
	}

	public void setUserToolChangeStartGcode(String userToolChangeStartGcode) {
		this.userToolChangeStartGcode = userToolChangeStartGcode;
	}

	public String getUserGeneralStartGcode() {
		return userGeneralStartGcode;
	}

	public String getUserGeneralEndGcode() {
		return userGeneralEndGcode;
	}

	/**
	 * To resolve and evaluate user's personal start/end gcodes.
	 * <br>
	 * Assume preferences are up to date ...
	 * <br>
	 * <pre>{@code G0 X{limit_left+10} Y{limit_top-20} ; Park position}</pre>
	 * should be resolved and evaluated in something like
	 * <pre>{@code G0 X-315.0 Y480.0 ; Park position}</pre>
	 * <br>
	 * Expression evaluation thanks to
	 * https://stackoverflow.com/questions/2605032/is-there-an-eval-function-in-java
	 * :: https://stackoverflow.com/users/4244130/vincelomba :
	 * https://stackoverflow.com/posts/48251395/revisions
	 *
	 * @param in the String with expression to resolv and evaluate
	 * @return the result.
	 */
	public String resolvePlaceHolderAndEvalExpression(String in, PlotterSettings plotterSettings) {
		StringBuilder res = new StringBuilder();
		// build a simple map PlaceHolderName to current value ...
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(plotterSettings.getUID()));
		logger.trace("{}", thisMachineNode.toString());
		TreeMap<String, String> paramNameToValue = new TreeMap<>();
		try {
			String[] childrenNames = thisMachineNode.childrenNames();
			if (childrenNames != null) {
				logger.trace(" childrenNames:{}", childrenNames.length);
				// TODO if we want to use there sub values  ...
			}
		} catch (Exception e) {
			logger.trace("{}", e.getMessage(), e);
		}
		try {
			String[] keys = thisMachineNode.keys();
			if (keys != null) {
				System.out.println(" keys: " + keys.length);
				for (String key : keys) {
					logger.debug("  {}:{}", key, thisMachineNode.get(key, ""));
					paramNameToValue.put(key, thisMachineNode.get(key, ""));
				}
				/* TODO a doc with all the replacement pattern
				keys: 22
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   acceleration:100.0
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   blockBufferSize:16
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   handleSmallSegments:false
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   hardwareVersion:Makelangelo 5
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   isRegistered:false
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   limit_bottom:-500.0
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   limit_left:-325.0
				2022-02-25 19:01:38,809 DEBUG c.m.m.p.settings.PlotterSettings -   limit_right:325.0
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   limit_top:500.0
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   minAcceleration:0.0
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   minSegTime:20000
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   minSegmentLength:0.5
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   minimumPlannerSpeed:0.05
				2022-02-25 19:01:38,810 DEBUG c.m.m.p.settings.PlotterSettings -   paperColorB:255
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   paperColorG:255
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   paperColorR:255
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   segmentsPerSecond:5
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   startingPosIndex:4
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   userGeneralEndGcode:
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   userGeneralStartGcode:G28;
				G0 X{limit_left+10} Y{limit_top-20} ; Park position
				M300;
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   userToolChangeEndGcode:
				2022-02-25 19:01:38,811 DEBUG c.m.m.p.settings.PlotterSettings -   userToolChangeStartGcode:
				 */
			}
		} catch (Exception e) {
			logger.trace("{}", e.getMessage(), e);
		}
		// a parser like to replace placeHolder with ther value and to performe expression evalutation
		// https://github.com/MarginallyClever/Makelangelo-software/issues/552#issuecomment-1041907446 : I think I just parked it 10cm from the left edge and 15cm from the top edge.
		// TODO array [0] ... ?
		// todo conditional ?
		// As there is no need for a real parser (I'm not going to check that the whole thing respects a grammar)
		int readPos = 0;
		final char toResolvExpressionStartDelimitor = '{';
		final char toResolvExpressionEndDelimitor = '}';
		int level = 0; // To resolve and evaluate only if in a delimited expression.
		StringBuilder cumulTokenToEvaluate = new StringBuilder();
		while (readPos < in.length()) {
			char currentCharAtReadPos = in.charAt(readPos);
			switch (currentCharAtReadPos) {
				case toResolvExpressionStartDelimitor:
					level++;
					break;
				case toResolvExpressionEndDelimitor:
					level--;
					// TODO ? if nested
					// TODO a more elegant way to do this ... as the solveNumericExpression can get a map of variable name , value ?
					for (String k : paramNameToValue.keySet()) {
						String tmp = cumulTokenToEvaluate.toString().replaceAll(k, paramNameToValue.get(k));
						cumulTokenToEvaluate.setLength(0);
						cumulTokenToEvaluate.append(tmp);
					}
					try {
						double resTmp = FunctionSolver.solveNumericExpression(cumulTokenToEvaluate.toString());
						cumulTokenToEvaluate.setLength(0);
						cumulTokenToEvaluate.append(resTmp);
					} catch (Exception e) {
						logger.trace("{}", e.getMessage(), e);
					}
					res.append(cumulTokenToEvaluate);
					cumulTokenToEvaluate.setLength(0);
					break;
				default:
					if (level > 0) {
						cumulTokenToEvaluate.append(currentCharAtReadPos);
					} else {
						res.append(currentCharAtReadPos);
					}
			}
			readPos++;
		}
		return res.toString();
	}

	void saveUserGcode(Preferences thisMachineNode) {
		thisMachineNode.put("userGeneralStartGcode", userGeneralStartGcode);
		thisMachineNode.put("userGeneralEndGcode", userGeneralEndGcode);
		thisMachineNode.put("userToolChangeStartGcode", userToolChangeStartGcode);
		thisMachineNode.put("userToolChangeEndGcode", userToolChangeEndGcode);
	}

	public String getUserToolChangeStartGcode() {
		return userToolChangeStartGcode;
	}

	public void setUserGeneralEndGcode(String userGeneralEndGcode) {
		this.userGeneralEndGcode = userGeneralEndGcode;
	}

	public void setUserToolChangeEndGcode(String userToolChangeEndGcode) {
		this.userToolChangeEndGcode = userToolChangeEndGcode;
	}

	public String getUserToolChangeEndGcode() {
		return userToolChangeEndGcode;
	}

	void loadUserGcode(Preferences thisMachineNode) {
		userGeneralStartGcode = thisMachineNode.get("userGeneralStartGcode", userGeneralStartGcode);
		userGeneralEndGcode = thisMachineNode.get("userGeneralEndGcode", userGeneralEndGcode);
		userToolChangeStartGcode = thisMachineNode.get("userToolChangeStartGcode", userToolChangeStartGcode);
		userToolChangeEndGcode = thisMachineNode.get("userToolChangeEndGcode", userToolChangeEndGcode);
	}
	
}
