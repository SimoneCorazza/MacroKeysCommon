package com.macrokeys.screen;

import java.util.Objects;

import com.macrokeys.rendering.RectF;

/** Classe statica per utility relative allo schermo */
public final class ScreenUtility {
	
	private ScreenUtility() { }
	
    /** 1 pollice in millimetri */
    private static final float INCH_MM = 25.4f;
	
    /**
     * Converte millimetri in pixel per l'asse X
     * @param mm Millimetri da convertire
     * @param s Schermo di riferimento
     * @return Numero di pixel presenti nei millimetri indicati sull'asse X del display ({@code mm)
     * @throws NullPointerException Se {@code s} è null
     */
    public static float mmtopx_X(float mm, Screen s) {
    	Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getXDpi();
    }

    /**
     * Converte millimetri in pixel per l'asse Y
     * @param mm Millimetri da convertire
     * @param s Schermo di riferimento
     * @return Numero di pixel presenti nei millimetri indicati sull'asse Y del display ({@code mm})
     * @throws NullPointerException Se {@code s} è null
     */
    public static float mmtopx_Y(float mm, Screen s) {
    	Objects.requireNonNull(s);
        return (mm / INCH_MM) * s.getYDpi();
    }
    
    /**
     * Converte pixel in millimetri per l'asse X
     * @param px Pixel da convertire
     * @param s Schermo di riferimento
     * @return Numero di millimetri corspondenti ai pixel dello schermo sull'asse X
     * @throws NullPointerException Se {@code s} è null
     */
    public static float pxtomm_X(float px, Screen s) {
    	Objects.requireNonNull(s);
    	return (px / s.getXDpi()) * INCH_MM;
    }
    
    /**
     * Converte pixel in millimetri per l'asse Y
     * @param px Pixel da convertire
     * @param s Schermo di riferimento
     * @return Numero di millimetri corspondenti ai pixel dello schermo sull'asse Y
     * @throws NullPointerException Se {@code s} è null
     */
    public static float pxtomm_Y(float px, Screen s) {
    	Objects.requireNonNull(s);
    	return (px / s.getYDpi()) * INCH_MM;
    }

    /**
     * Converte un rettangolo in pixel
     * @param r Rettangolo in millimetri da convertire in pixel
     * @param s Schermo al quale adeguare le dimensioni
     * @return Rettangolo in pixel
     * @throws NullPointerException Se {@code s} o {@code r} sono null
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
