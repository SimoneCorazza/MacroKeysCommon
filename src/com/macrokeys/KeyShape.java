package com.macrokeys;

import java.io.Serializable;

/**
 * Shape of a UI key
 */
public final class KeyShape implements Cloneable, Serializable {

    /**
     * Serial for {@link Serializable}
     */
    private static final long serialVersionUID = 1L;

    /**
     * Type of the shape
     */
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
        if (obj instanceof KeyShape) {
            KeyShape s = (KeyShape) obj;
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
     * @return the Shape type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the shape type
     * @param type the Shape type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Type of shape of a key
     */
    public enum Type {

        Rectangle, Ellipse
    }
}
