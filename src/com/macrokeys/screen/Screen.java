package com.macrokeys.screen;


/** Class that abstract a screen */
public abstract class Screen {
	
	/**
	 * Init in the main screen if possible
	 */
	public Screen() { }
	
	/**
	 * @return Dpi in the X axis of the screen
	 */
	public abstract float getXDpi();
	
	/**
	 * @return Dpi in the T axis of the screen
	 */
	public abstract float getYDpi();
	
}
