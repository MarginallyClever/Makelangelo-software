package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.actions.LoadFileAction;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A list of recent files displayed as a JMenu.  The list can be saved to and loaded from preferences.
 */
public final class RecentFiles extends JMenu {
	private static final Logger logger = LoggerFactory.getLogger(RecentFiles.class);

	public final int MAX_FILES = 10;

	@SuppressWarnings("deprecation")
	private final Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	private final List<String> fileList = new ArrayList<>();
	private final MainFrame frame;

	// Load recent files from prefs
	public RecentFiles(String label,MainFrame frame) {
		super(label);
		this.frame = frame;
		loadFromStorage();
		updateLists();
	}

	/**
	 * Adds a filename to the recent files list, saves the updated prefs, and refreshes the menus.
	 * Changes the order of the recent files with the new item at the top.  If the item was already in the list
	 * It is moved to the top.
	 * @param filename the file to push to the top of the list.
	 */
	public void addFilename(String filename) {
		if(filename==null || filename.trim().isEmpty()) return;
		
		int i = getIndexOf(filename);
		if(i==-1) {
			fileList.addFirst(filename);
		} else {
			// bump to the head of the list
			fileList.addFirst(fileList.remove(i));
		}
		updateLists();
	}

	/**
	 * removes a filename from the recent files list, saves the updated prefs, and refreshes the menus.
	 * @param filename
	 */
	public void removeFilename(String filename) {
		int i = getIndexOf(filename);
		if(i==-1) return;

		fileList.remove(i);
		updateLists();
	}

	private int getIndexOf(String filename) {
		int i=0;
		for( String j : fileList ) {
			if(j.contentEquals(filename)) {
				return i;
			}
			++i;
		}
		return -1;
	}

	public int getMaxFiles() {
		return MAX_FILES;
	}

	public String getFile(int index) {
		return fileList.get(index);
	}

	private void updateLists() {
		this.removeAll();

		for(int i=0;i<MAX_FILES;++i) {
			prefs.remove(getNodeName(i));
		}
		
		int i=0;
		for( String f : fileList ) {
			prefs.put(getNodeName(i++), f);
			this.add(new LoadFileAction(f,frame,this));
		}
	}

	private void loadFromStorage() {
		logger.debug("loading recent files");
		for (int i=0; i<MAX_FILES; ++i) {
			String name = getNodeName(i);
			String value = prefs.get(name, "");
			if (!value.trim().isEmpty()) {
				logger.trace("...{}", value);
				fileList.add(value);
			}
		}
	}

	private String getNodeName(int i) {
		return "recent-files-"+i;
	}
}
