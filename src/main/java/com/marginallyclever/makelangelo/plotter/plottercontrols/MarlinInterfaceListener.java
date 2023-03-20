package com.marginallyclever.makelangelo.plotter.plottercontrols;

/**
 * Anyone who implements MarlinInterfaceListener is listening to eventss coming from {@link MarlinInterface}.
 */
public interface MarlinInterfaceListener {
	void actionPerformed(MarlinInterfaceEvent e);
}
