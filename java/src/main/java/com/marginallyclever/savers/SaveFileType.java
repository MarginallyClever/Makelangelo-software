package com.marginallyclever.savers;

import javax.swing.filechooser.FileNameExtensionFilter;

public interface SaveFileType {
	/**
	 * Just what it sounds like.
	 * @return
	 */
	public FileNameExtensionFilter getFileNameFilter();

	
	public void save(String filename);
}
