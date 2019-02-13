package com.macrokeys;

import java.io.Serializable;

/**
 * Forma che un tasto nell'UI può avere
 */
public final class KeyShape implements Cloneable, Serializable {
	

    /**
	 * Seriale per {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	/** Tipologia della forma del tasto */
    private Type type;

    public KeyShape() {
        type = Type.Rectangle;
    }

    public KeyShape(Type t) {
        type = t;
    }

    
    
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof KeyShape) {
    		KeyShape s = (KeyShape)obj;
    		return s.getType().equals(getType());
    	} else {
    		return false;
    	}
    }
    
    @Override
    public int hashCode() {
    	return type.hashCode();
    }

    /**
     * @return Tipologia della forma del testo
     */
    public Type getType() {
        return type;
    }

    /**
     * Setta la tipologia della forma del testo
     * @param type - Tipologia di forma
     */
    public void setType(Type type) {
        this.type = type;
    }


    /** Tipologia di forma del tasto */
    public enum Type {
        Rectangle,
        Ellipse,
    }
}
