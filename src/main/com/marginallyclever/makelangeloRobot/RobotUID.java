package com.marginallyclever.makelangeloRobot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;

/**
 * Was used in Makelangelo-firmware to get the GUID from the arduino board.
 * If the number was zero, it would request a new number from the Marginally Clever url.
 * This way Marginally Clever could track how many new machines were being made over time.
 * Kept for historical reasons.
 * @author Dan Royer
 */
@Deprecated
public class RobotUID {
	public static long findOrCreateUID(String line) {
		long newUID = -1;
		
		// try to get the UID in the line
		String[] lines = line.split("\\r?\\n");
		if (lines.length > 0) {
			try {
				newUID = Long.parseLong(lines[0]);
				Log.message("UID found: "+newUID);
			} catch (NumberFormatException e) {
				Log.error("UID parsing failed. line="+lines[0]+", error="+e.getLocalizedMessage());
			}
		}

		// new robots have UID<=0
		if(newUID <= 0) newUID = getNewRobotUID();
		
		return newUID;
	}

	// based on http://www.exampledepot.com/egs/java.net/Post.html
	private static long getNewRobotUID() {
		long newUID = 0;

		boolean pleaseGetAGUID = !CommandLineOptions.hasOption("-noguid");
		if (!pleaseGetAGUID)
			return 0;

		Log.message("obtaining UID from server.");
		try {
			// Send request
			URL url = new URL("https://www.marginallyclever.com/drawbot_getuid.php");
			URLConnection conn = url.openConnection();
			// get results
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = rd.readLine();
			Log.message("Server says: '" + line + "'");
			newUID = Long.parseLong(line);
			if (newUID != 0) {
				//settings.createNewUID(newUID);
			}
			
		} catch (Exception e) {
			Log.error("UID from server: " + e.getMessage());
			return 0;
		}

		return newUID;
	}
	
}
