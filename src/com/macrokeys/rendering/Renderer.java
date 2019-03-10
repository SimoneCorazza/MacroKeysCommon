package com.macrokeys.rendering;

/**
 * Object rendering
 */
public interface Renderer {

    /**
     * Set the new color rendering
     * @param argb the New color
     */
    void setColor(int argb);

    /**
     * Sets the anti-aliasing
     * @param aa Active disable the AA
     */
    void setAntiAlias(boolean aa);

    /**
     * Sets the mode of rendering
     * @param p the Way of rendering
     * @throws NullPointerException If {@code p} is null
     */
    void setPaintStyle(PaintStyle p);

    /**
     * Sets the alignment of the text
     * @param t
     * @throws NullPointerException If {@code t} is null
     */
    void setTextAllign(TextAllign t);

    /**
     * Sets the height of the text
     * @param textSize the text Height
     */
    void setTextSize(float textSize);

    /**
     * Renders a ellipse/oval into the specified area
     * @param Area
     * @throws NullPointerException If {@code a} is null
     */
    void ellipse(RectF a);

    /**
     * Renders a rectangle in the area indicated
     * @param Area
     * @throws NullPointerException If {@code a} is null
     */
    void rect(RectF a);

    /**
     * Render the string s in the specified coordinates with the set set
     * @param s the String to render
     * @param r the Area where to place the string
     * @throws NullPointerException If {@code s}, or {@code r} are null
     */
    void text(String s, RectF r);
}
