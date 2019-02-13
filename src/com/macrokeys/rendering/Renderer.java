package com.macrokeys.rendering;

/** Oggetto di rendering */
public interface Renderer {
	
	/**
	 * Imposta il nuovo colore di rendering
	 * @param argb Nuovo colore
	 */
	void setColor(int argb);
	
	/**
	 * Imposta l'anti aliasing
	 * @param aa Attiva disattiva l'AA
	 */
	void setAntiAlias(boolean aa);
	
	/**
	 * Imposta il modo di rendering
	 * @param p Modo di rendering
	 * @throws NullPointerException Se {@code p} è null
	 */
	void setPaintStyle(PaintStyle p);
	
	/**
	 * Imposta l'allineamento del testo 
	 * @param t
	 * @throws NullPointerException Se {@code t} è null
	 */
	void setTextAllign(TextAllign t);
	
	/**
	 * Imposta l'altezza del testo
	 * @param textSize Altezza del testo; > 0
	 */
	void setTextSize(float textSize);
	
	/**
	 * Renderizza un ellisse/ovale nell'area indicata
	 * @param a Area
	 * @throws NullPointerException Se {@code a} è null
	 */
	void ellipse(RectF a);
	
	/**
	 * Renderizza un rettangolo nell'area indicata
	 * @param a Area
	 * @throws NullPointerException Se {@code a} è null
	 */
	void rect(RectF a);
	
	/**
	 * Renderizza la stringa s nelle coordinate indicate con lo settato impostato
	 * @param s Stringa da renderizzare
	 * @param r Area dove posizionare la stringa
	 * @throws NullPointerException Se {@code s} o {@code r} sono null
	 */
	void text(String s, RectF r);
}
