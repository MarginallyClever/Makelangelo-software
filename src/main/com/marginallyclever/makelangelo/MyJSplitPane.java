package com.marginallyclever.makelangelo;

import javax.swing.JSplitPane;

// manages the vertical split in the GUI
public class MyJSplitPane extends JSplitPane {
  static final long serialVersionUID = 1;

  public MyJSplitPane(int split_direction) {
    super(split_direction);
    setResizeWeight(0.95);
    setDividerLocation(0.95);
  }
}
