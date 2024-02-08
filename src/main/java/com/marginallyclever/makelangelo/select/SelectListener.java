package com.marginallyclever.makelangelo.select;

import java.util.EventListener;

/**
 * A SelectEvent is fired when a Select changes value.
 * @author Dan Royer
 * @since 7.50.2
 */
public interface SelectListener extends EventListener {
    void selectEvent(SelectEvent evt);
}
