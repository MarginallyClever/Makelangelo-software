/*
 */
package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.util.FunctionSolver;
import com.marginallyclever.util.PreferencesHelper;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PlotterSettingsUserGcode {

	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsUserGcode.class);

	private String userGeneralStartGcode = """
										   M220 S100 ; Set speed percentage factor to 100%
										   G28 ; Auto Home"""; // DONE but TODO in PlotterControl Should containe a G28 or a G28 is added befor any move...
	private String userGeneralEndGcode = """
										 G0 X{limit_left+100} Y{limit_top-150} ; Park position
										 M300 ; Play Tone
										 M18 ; Disable all steppers immediately"""; // TODO Should containe a disble motors or added at the end //  I think I just parked it 10cm from the left edge and 15cm from the top edge.
	private String userToolChangeStartGcode = "M300 ; Play Tone"; // Should go in relative moves befor and in absolut moves after ??? a save position ?G60 - Save Current Position https://marlinfw.org/docs/gcode/G060.html
	private String userToolChangeEndGcode = ""; // a resort position ? G61 - Return to Saved Position https://marlinfw.org/docs/gcode/G061.html

	public static final String PREF_KEY_USER_TOOL_CHANGE_END_GCODE = "userToolChangeEndGcode";
	public static final String PREF_KEY_USER_TOOL_CHANGE_START_GCODE = "userToolChangeStartGcode";
	public static final String PREF_KEY_USER_GENERAL_END_GCODE = "userGeneralEndGcode";
	public static final String PREF_KEY_USER_GENERAL_START_GCODE = "userGeneralStartGcode";

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
	
	public String[] getCommentCleanned(String sUserGcode,PlotterSettings myPlotterSettings){
		String[] userGcode = resolvePlaceHolderAndEvalExpression(sUserGcode,myPlotterSettings).split("\n");	
		ArrayList<String> tmp = new ArrayList<>();				
		for ( String l : userGcode){
			String[] lPart = l.split(";");// to separate the comments
			if ( lPart!= null && lPart.length>0 ){
				if ( lPart[0]!= null && lPart[0].length()>0){					
					tmp.add(lPart[0]);
				}
			}
		}
		return tmp.toArray(String[]::new);
	}

	/**
	 * To resolve and evaluate user's personal start/end gcodes.<br>
	 * Assume preferences are up to date ...<br>
	 * <pre>{@code G0 X{limit_left+10} Y{limit_top-20} ; Park position}</pre>
	 * should be resolved and evaluated in something like
	 * <pre>{@code G0 X-315.0 Y480.0 ; Park position}</pre>
	 *
	 * @param in the String with expression to resolv and evaluate
	 * @param plotterSettings
	 * @return the result.
	 */
	public String resolvePlaceHolderAndEvalExpression(String in, PlotterSettings plotterSettings) {
		StringBuilder res = new StringBuilder();
		Map<String, String> paramNameToValue = createPatternToValueMap(plotterSettings);
		// a parser like to replace placeHolder with ther value and to performe expression evalutation
		// TODO array [0] ... ? if need array, the map should have keys like array[0], array[1] and associated value ?
		// conditionals, need a real parser / interpreter (variables / stack ) ... not done in this implementation.
		// lazzy way : not going to check that the whole thing respects a grammar !
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

					// resolve // TODO ? a more elegant way to do this ...
					for (String k : paramNameToValue.keySet()) {
						String tmp = cumulTokenToEvaluate.toString().replaceAll(k, paramNameToValue.get(k));
						cumulTokenToEvaluate.setLength(0);
						cumulTokenToEvaluate.append(tmp);
					}

					// evaluate
					try {
						double resTmp = FunctionSolver.solveNumericExpression(cumulTokenToEvaluate.toString());
						cumulTokenToEvaluate.setLength(0);
						cumulTokenToEvaluate.append(resTmp);
					} catch (Exception e) {
						// evalutation faild but maybe this is not an expression to evaluate ...
						// as with some 3d printer slicer, the user have to check the obtained .gcode file ...
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

	public String getAllPattern(PlotterSettings plotterSettings) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> paramNameToValue = createPatternToValueMap(plotterSettings);
		Set<String> keySet = paramNameToValue.keySet();
		for (String k : keySet) {
			sb.append("{").append(k).append("} ; ").append(paramNameToValue.get(k)).append("\n");
		}
		return sb.toString();
	}

	/**
	 * TODO to review BUG on the first run (or if you have deleted your prefrence file) this give an empty list ... as this Prefrence node have not been init if the Plotter settings panel have not been load ...
	 * @param plotterSettings
	 * @return 
	 */
	private Map<String, String> createPatternToValueMap(PlotterSettings plotterSettings) {

		// build a simple map PlaceHolderName to current value ...
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(plotterSettings.getUID()));
		logger.trace("{}", thisMachineNode.toString());

		Map<String, String> paramNameToValue = new TreeMap<>();

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
				logger.trace(" keys count: {}", keys.length);
				for (String key : keys) {
					logger.trace("  {}:{}", key, thisMachineNode.get(key, ""));
					// TODO to review maybe a TreeSet of key to exclude ( or a TreeSet of key to keep )
					if (!PREF_KEY_USER_GENERAL_END_GCODE.equals(key)
							&& !PREF_KEY_USER_GENERAL_START_GCODE.equals(key)
							&& !PREF_KEY_USER_TOOL_CHANGE_START_GCODE.equals(key)
							&& !PREF_KEY_USER_TOOL_CHANGE_END_GCODE.equals(key) 
							
							&& !PlotterSettings.PREF_KEY_ACCELERATION.equals(key) 
							&& !PlotterSettings.PREF_KEY_BLOCK_BUFFER_SIZE.equals(key) 
							&& !PlotterSettings.PREF_KEY_HANDLE_SMALL_SEGMENTS.equals(key) 
							&& !PlotterSettings.PREF_KEY_IS_REGISTERED.equals(key) 
														
							&& !PlotterSettings.PREF_KEY_MIN_ACCELERATION.equals(key) 
							&& !PlotterSettings.PREF_KEY_MIN_SEG_TIME.equals(key) 
							&& !PlotterSettings.PREF_KEY_MIN_SEGMENT_LENGTH.equals(key) 
							&& !PlotterSettings.PREF_KEY_MINIMUM_PLANNER_SPEED.equals(key)
							
							&& !PlotterSettings.PREF_KEY_PAPER_COLOR_R.equals(key) 
							&& !PlotterSettings.PREF_KEY_PAPER_COLOR_G.equals(key) 
							&& !PlotterSettings.PREF_KEY_PAPER_COLOR_B.equals(key)  
							
							&& !PlotterSettings.PREF_KEY_SEGMENTS_PER_SECOND.equals(key) 
							&& !PlotterSettings.PREF_KEY_STARTING_POS_INDEX.equals(key) 
							) {
						paramNameToValue.put(key, thisMachineNode.get(key, ""));
					}
				}
			}
		} catch (Exception e) {
			logger.trace("{}", e.getMessage(), e);
		}
		return paramNameToValue;
	}

	void saveUserGcode(Preferences thisMachineNode) {
		thisMachineNode.put(PREF_KEY_USER_GENERAL_START_GCODE, userGeneralStartGcode);
		thisMachineNode.put(PREF_KEY_USER_GENERAL_END_GCODE, userGeneralEndGcode);
		thisMachineNode.put(PREF_KEY_USER_TOOL_CHANGE_START_GCODE, userToolChangeStartGcode);
		thisMachineNode.put(PREF_KEY_USER_TOOL_CHANGE_END_GCODE, userToolChangeEndGcode);
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
		userGeneralStartGcode = thisMachineNode.get(PREF_KEY_USER_GENERAL_START_GCODE, userGeneralStartGcode);
		userGeneralEndGcode = thisMachineNode.get(PREF_KEY_USER_GENERAL_END_GCODE, userGeneralEndGcode);
		userToolChangeStartGcode = thisMachineNode.get(PREF_KEY_USER_TOOL_CHANGE_START_GCODE, userToolChangeStartGcode);
		userToolChangeEndGcode = thisMachineNode.get(PREF_KEY_USER_TOOL_CHANGE_END_GCODE, userToolChangeEndGcode);
	}

	public boolean doUserStartGCodeContaineG28(PlotterSettings plotterSettings){
		String[] commentCleanned = getCommentCleanned(userGeneralStartGcode, plotterSettings);
		if ( commentCleanned != null && commentCleanned.length>0){
			for ( String l : commentCleanned){
				if ( l.startsWith("G28")){// as normaly no G28x exist ... 
					return true;
				}
			}
		}
		return false;
	}
	
}
