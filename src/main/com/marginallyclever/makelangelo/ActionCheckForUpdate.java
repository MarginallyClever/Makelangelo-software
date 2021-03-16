package com.marginallyclever.makelangelo;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.log.Log;


/**
 * private void downloadUpdate() {
 *   String[] run = {
 *     "java",
 *     "-jar",
 *     "updater/update.jar"
 *   };
 *   try {
 *     Runtime.getRuntime().exec(run);
 *   } catch (Exception ex) {
 *     ex.printStackTrace();
 *   }
 *   System.exit(0);
 * }
 * 
 * @See <a href='http://www.dreamincode.net/forums/topic/190944-creating-an-updater-in-java/'>More here</a>
 */
public class ActionCheckForUpdate extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2057612555824936340L;
	private Frame myFrame;
	private String myVersion;

	public ActionCheckForUpdate(Frame mainFrame,String version) {
		super(Translator.get("MenuUpdate"));	
		myFrame = mainFrame;
		myVersion = version;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		checkForUpdate(false);
	}


	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest
	 * redirect notice to find the latest release tag.
	 */
	public void checkForUpdate(boolean announceIfFailure) {
		Log.message("checking for updates...");
		try {
			URL github = new URL("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false); // you still need to handle redirect manually.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine = in.readLine();
			if (inputLine == null) {
				throw new Exception("Could not read from update server.");
			}

			// parse the URL in the text-only redirect
			String matchStart = "<a href=\"";
			String matchEnd = "\">";
			int start = inputLine.indexOf(matchStart);
			int end = inputLine.indexOf(matchEnd);
			if (start != -1 && end != -1) {
				String line2 = inputLine.substring(start + matchStart.length(), end);
				// parse the last part of the redirect URL, which contains the
				// release tag (which is the VERSION)
				line2 = line2.substring(line2.lastIndexOf("/") + 1);

				Log.message("latest release: " + line2 + "; this version: " + myVersion);
				// Log.message(inputLine.compareTo(VERSION));

				int comp = line2.compareTo(myVersion);
				String results;
				if (comp > 0) {
					results = Translator.get("Makelangelo.updateNotice");
					// TODO downloadUpdate(), updateThisApp();
				} else if (comp < 0)
					results = "This version is from the future?!";
				else
					results = Translator.get("Makelangelo.upToDate");

				JOptionPane.showMessageDialog(myFrame, results);
			}
			in.close();
		} catch (Exception e) {
			if (announceIfFailure) {
				JOptionPane.showMessageDialog(null, Translator.get("Makelangelo.updateCheckFailed"));
			}
			e.printStackTrace();
		}
	}
}
