package com.macrokeys.rendering;

import org.eclipse.jdt.annotation.NonNull;

/** Rendering interface for the macrokeys */
public interface Renderer {
	
	/**
	 * Sets the rencering color
	 * @param argb Color
	 */
	void setColor(int argb);
	
	/**
	 * Sets the anti aliasing
	 * @param aa True active, false inactive
	 */
	void setAntiAlias(boolean aa);
	
	/**
	 * Sets the rendering method
	 * @param p Rendering method
	 */
	void setPaintStyle(@NonNull PaintStyle p);
	
	/**
	 * Sets text allignment
	 * @param t Text allignment
	 */
	void setTextAllign(@NonNull TextAllign t);
	
	/**
	 * Sets the text height
	 * @param textSize Text height; > 0
	 */
	void setTextSize(float textSize);
	
	/**
	 * Render an ellipse in the given area
	 * @param a Area
	 */
	void ellipse(@NonNull RectF a);
	
	/**
	 * Render a rectangle in the given area
	 * @param a Area
	 */
	void rect(@NonNull RectF a);
	
	/**
	 * Renders a string in the given area
	 * @param s String to render
	 * @param r Area where place the string
	 */
	void text(@NonNull String s, @NonNull RectF r);
}
