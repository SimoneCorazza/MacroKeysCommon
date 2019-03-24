package com.macrokeys.screen;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.rendering.RectF;

/** Static class for screen utilities */
public final class ScreenUtility {
	
	private ScreenUtility() { }
	
    /** 1 inch in millimeters */
    private static final float INCH_MM = 25.4f;
	
    /**
     * Converts millimeters in pixels for the X axis
     * @param mm Millimeters to convert
     * @param s Screen to use
     * @return Conversion result
     */
    public static float mmtopx_X(float mm, @NonNull Screen s) {
    	Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getXDpi();
    }

    /**
     * Converts millimeters in pixels for the Y axis
     * @param mm Millimeters to convert
     * @param s Screen to use
     * @return Conversion result
     */
    public static float mmtopx_Y(float mm, @NonNull Screen s) {
    	Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getYDpi();
    }
    
    /**
     * Converts pixels in millimeters for the X axis
     * @param px Pixels to convert
     * @param s Screen to use
     * @return Conversion result
     */
    public static float pxtomm_X(float px, @NonNull Screen s) {
    	Objects.requireNonNull(s);
    	return (px / s.getXDpi()) * INCH_MM;
    }
    
    /**
     * Converts pixels in millimeters for the Y axis
     * @param px Pixels to convert
     * @param s Screen to use
     * @return Conversion result
     */
    public static float pxtomm_Y(float px, Screen s) {
    	Objects.requireNonNull(s);
    	return (px / s.getYDpi()) * INCH_MM;
    }

    /**
     * Converts a rectangle in pixels
     * @param r Reactangle to convert
     * @param s Screen to use
     * @return Rectangle in pixels
     */
    public static RectF mmtopx(@NonNull RectF r, @NonNull Screen s) {
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
