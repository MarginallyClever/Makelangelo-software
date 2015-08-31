package com.marginallyclever.makelangelo;

import java.util.prefs.Preferences;

public final class RecentFiles<P extends Preferences> {
  @SuppressWarnings("deprecation")
  private P prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
  private String[] fileList;
  final int MAX_FILES = 10;

  /**
   * changes the order of the recent files list in the File submenu, saves the updated prefs, and refreshes the menus.
   *
   * @param filename the file to push to the top of the list.
   */
  public void add(String filename) {
    String[] newFiles = new String[fileList.length];

    newFiles[0] = filename;

    int i, j = 1;
    for (i = 0; i < fileList.length; ++i) {
      if (!filename.equals(fileList[i]) && !fileList[i].equals("")) {
        newFiles[j++] = fileList[i];
        if (j == fileList.length) break;
      }
    }

    fileList = newFiles;

    // update prefs
    for (i = 0; i < fileList.length; ++i) {
      if (fileList[i] != null && !fileList[i].isEmpty()) {
        prefs.put("recent-files-" + i, fileList[i]);
      }
    }
  }


  public int getMaxFiles() {
    return fileList.length;
  }


  public String get(int index) {
    if (index < 0 || index >= fileList.length) return "";

    return fileList[index];
  }


  // A file failed to load.  Remove it from recent files, refresh the menu bar.
  public void remove(String filename) {
    int i;
    for (i = 0; i < fileList.length - 1; ++i) {
      if (fileList[i].equals(filename)) {
        break;
      }
    }
    for (; i < fileList.length - 1; ++i) {
      fileList[i] = fileList[i + 1];
    }
    fileList[fileList.length - 1] = "";

    // update prefs
    for (i = 0; i < fileList.length; ++i) {
      if (fileList[i] != null && !fileList[i].isEmpty()) {
        prefs.put("recent-files-" + i, fileList[i]);
      }
    }
    prefs.remove("recent-files-" + (i - 1));
  }

  // Load recent files from prefs
  public RecentFiles() {
    fileList = new String[MAX_FILES];

    int i;
    for (i = 0; i < fileList.length; ++i) {
      fileList[i] = prefs.get("recent-files-" + i, "");
    }
  }
}
