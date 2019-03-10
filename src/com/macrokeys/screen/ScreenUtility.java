package com.macrokeys.screen;

import java.util.Objects;
import com.macrokeys.rendering.RectF;

/**
 * Static class for utility relative to the screen
 */
public final class ScreenUtility {

    private ScreenUtility() {
    }

    /**
     * 1 inch in millimeters
     */
    private static final float INCH_MM = 25.4f;

    /**
     * It converts from millimeters to pixels for the X-axis
     * @param mm Millimeters convert
     * @param s Screen reference
     * @return the Number of pixels in millimeters indicated on the X-axis of the display ({@code mm)
     * @throws NullPointerException If {@code s} is null
     */
    public static float mmtopx_X(float mm, Screen s) {
        Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getXDpi();
    }

    /**
     * It converts from millimeters to pixels for the Y-axis
     * @param mm Millimeters convert
     * @param s Screen reference
     * @return the Number of pixels in millimeters indicated on the Y-axis of the display ({@code mm})
     * @throws NullPointerException If {@code s} is null
     */
    public static float mmtopx_Y(float mm, Screen s) {
        Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getYDpi();
    }

    /**
     * Converts pixels to millimeters for the X-axis
     * @param px the Pixel to be converted
     * @param s Screen reference
     * @return the Number of millimeters corspondenti to the pixels of the screen on the X-axis
     * @throws NullPointerException If {@code s} is null
     */
    public static float pxtomm_X(float px, Screen s) {
        Objects.requireNonNull(s);
        return (px / s.getXDpi()) * INCH_MM;
    }

    /**
     * Converts pixels to millimeters for the Y-axis
     * @param px the Pixel to be converted
     * @param s Screen reference
     * @return the Number of millimeters corspondenti to the pixels of the screen on the Y-axis
     * @throws NullPointerException If {@code s} is null
     */
    public static float pxtomm_Y(float px, Screen s) {
        Objects.requireNonNull(s);
        return (px / s.getYDpi()) * INCH_MM;
    }

    /**
     * Convert a rectangle in pixels
     * @param r the Rectangle, in millimeters, to convert to pixels
     * @param s the Screen to which to adapt the size
     * @return the Rectangle, in pixels
     * @throws NullPointerException If {@code s}, or {@code r} are null
     */
    public static RectF mmtopx(RectF r, Screen s) {
        Objects.requireNonNull(r);
        Objects.requireNonNull(s);
        RectF n = new RectF();
        n.left = mmtopx_X(r.left, s);
        n.top = mmtopx_Y(r.top, s);
        n.right = mmtopx_X(r.right, s);
        n.bottom = mmtopx_Y(r.bottom, s);
        return n;
    }
}
