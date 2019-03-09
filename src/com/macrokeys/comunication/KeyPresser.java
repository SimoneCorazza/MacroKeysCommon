package com.macrokeys.comunication;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import com.macrokeys.LimitedKeySequence;

/**
 * Handle the pressure and release of keys
 */
final class KeyPresser {

    private final Robot robot;

    /**
     * @throws AWTException If there is an error while initialising the {@link Robot}
     */
    public KeyPresser() throws AWTException {
        robot = new Robot();
    }

    /**
     * Press the given keys
     * @param s Sequence of keys to press
     */
    public synchronized void press(@NonNull LimitedKeySequence s) {
        Objects.requireNonNull(s);
        for (int i : s.getKeys()) {
            robot.keyPress(i);
        }
    }

    /**
     * Release the given keys
     * @param s Sequence of keys to release
     */
    public synchronized void release(@NonNull LimitedKeySequence s) {
        Objects.requireNonNull(s);
        for (int i : s.getKeys()) {
            robot.keyRelease(i);
        }
    }
}
