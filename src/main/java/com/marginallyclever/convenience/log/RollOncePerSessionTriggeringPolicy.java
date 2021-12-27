package com.marginallyclever.convenience.log;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

import java.io.File;

/**
 * Logback policy referenced in logback.xml in order to get a new file at each start of the application
 * @param <E>
 */
public class RollOncePerSessionTriggeringPolicy<E> extends TriggeringPolicyBase<E> {
    private static boolean doRolling = true;

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        // roll the first time when the event gets called
        if (doRolling) {
            doRolling = false;
            return true;
        }
        return false;
    }
}

