package com.macrokeys;

/**
 * Type of {@link MacroKey}
 */
public enum MacroKeyType {

    /**
     * The sequence is pressed and released when the {@link MacroKey} is
     * respectively pressed and released.
     * <br/>
     * Useful in videogames.
     */
    Game,
    /**
     * The sequence of keys is pressed and released periodically while
     * the {@link MacroKey} is pressed.
     * <br/>
     * Useful for macros and writing programs.
     */
    Normal,
    /**
     * The sequence of keys is pressed and released when the
     * {@link MacroKey} is released.
     * <br/>
     * Useful to avoid unwanted cliks.
     */
    OnRelease
}
