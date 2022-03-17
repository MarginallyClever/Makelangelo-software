package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public final class RecentFiles extends JMenu {
	private static final Logger logger = LoggerFactory.getLogger(RecentFiles.class);
	
	private static final long serialVersionUID = -4331360984016876093L;

	public final int MAX_FILES = 10;

	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	private ArrayList<String> fileList = new ArrayList<String>();
	private ActionListener submenuListener;

	// Load recent files from prefs
	public RecentFiles(String label) {
		super(label);
		
		loadFromStorage();
		updateLists();
	}

	/**
	 * changes the order of the recent files list in the File submenu, saves the
	 * updated prefs, and refreshes the menus.
	 *
	 * @param filename the file to push to the top of the list.
	 */
	public void addFilename(String filename) {
		if(filename==null || filename.trim().isEmpty()) return;
		
		int i = getIndexOf(filename);
		if(i==-1) {
			fileList.add(0,filename);
		} else {
			// bump to the head of the list
			fileList.add(0,fileList.remove(i));
		}
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

	public void removeFilename(String filename) {
		int i = getIndexOf(filename);
		if(i==-1) return;
		
		fileList.remove(i);
		updateLists();
	}

	private void updateLists() {
		this.removeAll();

		for(int i=0;i<MAX_FILES;++i) {
			prefs.remove(getNodeName(i));
		}
		
		int i=0;
		for( String f : fileList ) {
			prefs.put(getNodeName(i++), f);
			JMenuItem item = new JMenuItem(f);
			this.add(item);
			item.addActionListener(submenuListener);
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

	public void addSubmenuListener(ActionListener object) {
		submenuListener = object;
		updateLists();
	}
}
