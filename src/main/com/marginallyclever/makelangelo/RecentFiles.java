package com.marginallyclever.makelangelo;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;

public final class RecentFiles extends JMenu {
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
		reportStoredList();
		this.removeAll();

		for(int i=0;i<MAX_FILES;++i) {
			prefs.remove(getNodeName(i));
		}
		
		System.out.println("list:");
		int i=0;
		for( String f : fileList ) {
			System.out.println("  adding "+f);
			prefs.put(getNodeName(i++), f);
			JMenuItem item = new JMenuItem(f);
			this.add(item);
			item.addActionListener(submenuListener);
		}
		
	}
	
	private void reportStoredList() {
		System.out.println("prefs:");
		for(int i=0;i<MAX_FILES;++i) {
			System.out.println("..."+prefs.get(getNodeName(i), ""));
		}
	}
	
	private void loadFromStorage() {
		Log.message("loading recent files:");
		for(int i=0;i<MAX_FILES;++i) {
			String name = getNodeName(i);
			String value = prefs.get(name, "");
			if(!value.trim().isEmpty()) {
				Log.message("..."+value);
				fileList.add(value);
			} else {
				Log.message("...done");
				return;
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
