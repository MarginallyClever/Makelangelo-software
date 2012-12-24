import java.net.URL;
import java.net.URLConnection;
import java.util.prefs.Preferences;

/**
 * Send the total distance of line drawn to the website because people like metrics.
 * @author danroyer
 *
 */
public class DistanceMetric {
	static int robot_id=0;
	static float total_distance=0;
	static String query="";

	private Preferences prefs = Preferences.userRoot().node("DistanceMetric");
	
	
	DistanceMetric(int robot_uid) {
		robot_id=robot_uid;
		query="http://marginallyclever.com/drawbot_distance.php?uid="+robot_uid+"&len=";
		Load();
	}

	public void Save() {
		prefs.put("total_distance", Float.toString(total_distance));
	}
	
	public void Load() {
		total_distance=Float.parseFloat(prefs.get("total_distance","0"));
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
