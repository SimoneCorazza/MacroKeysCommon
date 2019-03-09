package com.macrokeys;

import java.io.Serializable;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.rendering.Color;
import com.macrokeys.rendering.RectF;
import com.macrokeys.screen.Screen;
import com.macrokeys.screen.ScreenUtility;

/**
 * Key present in a {@link MacroScreen}
 */
public final class MacroKey implements Cloneable, Serializable, Comparable<MacroKey> {
    /**
	 * Serial for {@link Serializable}
	 */
	private static final long serialVersionUID = 2L;

	/** Identifier of the key in the {@link MacroSetup} */
	private int id = -1;
	
	/** Sequence of keys associated with this macro key */
	private LimitedKeySequence macroSeq;
	
    /** Area occupied by this key at screen in millimiters; never null */
    private RectF area;
    
    /** Shape of the key; never null */
    private KeyShape shape;
    
    /** Color of the border; never null */
    private int colorEdge;
    
    /** Color of the fill; never null */
    private int colorFill;
    
    /** Color of the border when the key is pressed; never null */
    private int colorEdgePress;
    
    /** Color of the fill when the key is pressed; never null */
    private int colorFillPress;
    
    /** String of the test to render; never null */
    private String text;
    
    /** Type of key */
    private MacroKeyType type;


    /**
     * Instance a new key with default property
     */
    public MacroKey() {
    	setArea(new RectF(0, 0, 1, 1));
    	setShape(new KeyShape());
    	setColorEdge(Color.BLACK);
    	setColorFill(Color.GRAY);
    	setColorEdgePress(Color.BLACK);
    	setColorFillPress(Color.DKGRAY);
    	setText("Macro key");
    	setKeySeq(new LimitedKeySequence());
    	setType(MacroKeyType.Normal);
    }


    /**
	 * @return Identifier of the key; available only after the loading
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param Identifier of the key; >= 0
	 * @throws IllegalArgumentException If {@code id} is < 0
	 */
	void setId(int id) {
		if(id < 0) {
			throw new IllegalArgumentException("Parameter id must be >= 0");
		}
		this.id = id;
	}


	/**
     * @return Area (not editable) of the key at screen in millimiters; never null
     *   heigth and width always > 0
     */
    public RectF getArea() {
        return new RectF(area);
    }
    
    /**
     * @param s Screen for which obtein the area of this key
     * @return Area of the key at scrren in pixels
     * @throws NullPointerException If {@code s} is null
     */
    public @NonNull RectF getAreaPixel(@NonNull Screen s) {
    	Objects.requireNonNull(s);
    	return ScreenUtility.mmtopx(area, s);
    }

    /**
     * @return Shape of the key; is editable
     */
    public @NonNull KeyShape getShape() {
        return shape;
    }

    /**
     * @return Color of the border
     */
    public int getColorEdge() {
        return colorEdge;
    }

    /**
     * @return Color of the filling of the key
     */
    public int getColorFill() {
        return colorFill;
    }

    /**
     * @return Text of the key
     */
    public @NonNull String getText() {
        return text;
    }

    /**
     * @return Color of the border of the key when is pressed
     */
    public int getColorEdgePress() {
        return colorEdgePress;
    }

    /**
     * @return Color of the filling of the key when is pressed
     */
    public int getColorFillPress() {
        return colorFillPress;
    }
    
    /**
     * @param c Color of the filling
     */
    public void setColorFill(int c) {
    	colorFill = c;
    }

	/**
	 * @param area Area of the key at screen in millimiters; heigth e width sempre > 0
	 *  @throws IllegalArgumentException If the area of the key is 0
	 */
	public void setArea(@NonNull RectF area) {
		Objects.requireNonNull(area);
		if(area.width() < 0 || area.height() < 0) {
			throw new IllegalArgumentException("The width and heigth must be >= 0");
		}
		
		this.area = new RectF(area);
	}


	/**
	 * @param shape Shape of the key
	 */
	public void setShape(@NonNull KeyShape shape) {
		Objects.requireNonNull(shape);
		this.shape = shape;
	}


	/**
	 * @param colorEdge Color of the edge
	 */
	public void setColorEdge(int colorEdge) {
		this.colorEdge = colorEdge;
	}


	/**
	 * @param colorEdgePress Color of the edge when the key is pressed
	 */
	public void setColorEdgePress(int colorEdgePress) {
		this.colorEdgePress = colorEdgePress;
	}


	/**
	 * @param colorFillPress Color of the fiiling when the key is pressed
	 */
	public void setColorFillPress(int colorFillPress) {
		this.colorFillPress = colorFillPress;
	}


	/**
	 * @param text Text of the key
	 */
	public void setText(String text) {
		this.text = text == null ? "" : text;
	}
	
	
    /**
	 * @return Sequence of keys associeted with the key
	 */
	public @NonNull LimitedKeySequence getKeySeq() {
		return macroSeq;
	}

	/**
	 * @param keySeq Sequence of keys associeted with this
	 */
	public void setKeySeq(@NonNull LimitedKeySequence keySeq) {
		Objects.requireNonNull(keySeq);
		this.macroSeq = keySeq;
	}

	/**
	 * @return Key type
	 */
	public MacroKeyType getType() {
		return type;
	}


	/**
	 * @param keystrokeOnUp Key type
	 */
	public void setType(MacroKeyType type) {
		this.type = type;
	}

	@Override
    public Object clone() throws CloneNotSupportedException {
		return JavaUtil.utilDeepClone(this);
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof MacroKey)) {
			return false;
		} else {
			// Executing the cheks for every field not only the ID
			
			MacroKey k2 = (MacroKey)obj;
			return getId() == k2.getId() &&
				    getType() == k2.getType() &&
					getArea().equals(k2.getArea()) &&
					getColorEdge() == k2.getColorEdge() &&
					getColorEdgePress() == k2.getColorEdgePress() &&
					getColorFill() == k2.getColorFill() &&
					getColorFillPress() == k2.getColorFillPress() &&
					getKeySeq().equals(k2.getKeySeq()) &&
					getShape().equals(k2.getShape()) &&
					getText().equals(k2.getText());
		}
	}
	
	@Override
	public int hashCode() {
		return getId() + getArea().hashCode() + 
				getType().ordinal() +
				getColorEdge() +
				getColorEdgePress() + getColorFill() +
				getColorFillPress() + getKeySeq().hashCode() +
				getShape().hashCode() + getText().hashCode();
	}
	
	@Override
	public int compareTo(MacroKey m) {
		if(m == null) {
			return 0;
		} else {
			return Integer.compare(getId(), m.getId());
		}
	}
}
