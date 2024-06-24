package com.marginallyclever.makelangelo.apps.plottercontrols;

/**
 * Anyone who implements MarlinInterfaceListener is listening to eventss coming from {@link MarlinPanel}.
 */
public interface MarlinPanelListener {
	void actionPerformed(MarlinPanelEvent e);
}
