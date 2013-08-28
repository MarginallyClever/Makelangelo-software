import java.net.URL;
import java.net.URLConnection;
import java.util.prefs.Preferences;

/**
 * Send the total distance of line drawn to the website because people like metrics.
 * @author danroyer
 *
 */
public class DistanceMetric {
	static long robot_id=0;
	static float total_distance=0;
	static String query="";

	private Preferences prefs = Preferences.userRoot().node("DistanceMetric");
	
	
	DistanceMetric() {}
	
	public void SetUID(long robot_uid) {
		robot_id=robot_uid;
		query="http://marginallyclever.com/drawbot_distance.php?uid="+robot_uid+"&len=";
		Load();
	}

	public void Save() {
		prefs.put("total_distance", Float.toString(total_distance));
	}
	
	public void Load() {
		total_distance=Float.parseFloat(prefs.get("total_distance_"+robot_id,"0"));
	}
	
	public void SetDistance(float distance) {
		total_distance=distance;
		Save();
	}
	public float GetDistance() {
		return total_distance;
	}
	public void AddDistance(float distance) {
		total_distance+=distance;
		Save();
	}
	
	public void Report(float distance) {
		try {
		    // Send data
			URL url = new URL(query+distance);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (Exception e) {}
	}
}

/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */