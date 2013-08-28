import java.util.prefs.Preferences;
/*
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
*/

public class PublishImage {
	static String acct_name;
	static String acct_pass;
	static String acct_type;
	
	private Preferences prefs = Preferences.userRoot().node("PublishImage");
	
	
	public PublishImage() {
		Load();
	}
	
	public void SetTwitter(String newName, String newPass) {
		acct_type="twitter";
		acct_name=newName;
		acct_pass=newPass;
		Save();
	}
	
	public void Save() {
		prefs.put("acct_name", acct_name);
		prefs.put("acct_pass", acct_pass);
		prefs.put("acct_type", acct_type);
	}
	
	public void Load() {
		acct_pass=prefs.get("acct_pass","");
		acct_name=prefs.get("acct_name","");
		acct_type=prefs.get("acct_type","");		
	}
	
	public String GetName() { return acct_name; }
	public String GetPass() { return acct_pass; }
	
	public void Report() {/*
		try {
			Twitter twitter = TwitterFactory.getSingleton();
			//ConfigurationBuilder cb = twitter.getConfiguration();
			
			//cb.setOAuthConsumerKey("aLSfBmgmA4SFWE6Q3SEPw");
			//cb.setOAuthConsumerSecret("Li4LXSiLj13hWty2TEXMeQRX9pLnw9t7Lh29PaAis");
			//twitter.updateStatusWithFileMedia("This is a test",);
			//Status status = twitter.updateStatus("Hello, World!");
		} catch(TwitterException e) {}*/
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