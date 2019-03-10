package com.macrokeys.screen;

/**
 * Class that abstracts a screen in use
 */
public abstract class Screen {

    /**
     * Initializes on the main screen, if available
     */
    public Screen() {
    }

    /**
     * @return Dpi on the X-axis of the screen
     */
    public abstract float getXDpi();

    /**
     * @return Dpi for the Y axis of the screen
     */
    public abstract float getYDpi();
}
