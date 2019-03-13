package com.macrokeys.screen;


/** Classe che astrae uno schermo in uso */
public abstract class Screen {
	
	/**
	 * Inizializza sulo schermo principale, se disponibile
	 */
	public Screen() { }
	
	/**
	 * @return Dpi sull'asse X dello schermo
	 */
	public abstract float getXDpi();
	
	/**
	 * @return Dpi sull'asse Y dello schermo
	 */
	public abstract float getYDpi();
	
}
