package com.marginallyclever.makelangelo;

import javax.swing.JSplitPane;

// manages the vertical split in the GUI
public class Splitter extends JSplitPane {
  static final long serialVersionUID = 1;

  public Splitter(int split_direction) {
    super(split_direction);
    setResizeWeight(0.9);
    setDividerLocation(0.9);
  }
}
