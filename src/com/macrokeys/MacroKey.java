package com.macrokeys;

import java.io.Serializable;
import java.util.Objects;

import com.macrokeys.rendering.Color;
import com.macrokeys.rendering.RectF;
import com.macrokeys.screen.Screen;
import com.macrokeys.screen.ScreenUtility;

/**
 * Tasto presente in una MacroScreen, associato ad una macro
 */
public final class MacroKey implements Cloneable, Serializable, Comparable<MacroKey> {
    /**
	 * Seriale per {@link Serializable}
	 */
	private static final long serialVersionUID = 2L;

	/** Identificativo del tasto all'interno della {@link MacroSetup} */
	private int id = -1;
	
	/** Sequenza di pressioni associata alla pressione del tasto */
	private LimitedKeySequence macroSeq;
    /** Area occupata dal tasto in millimetri; non null */
    private RectF area;
    /** Forma del tasto; non null */
    private KeyShape shape;
    /** Colore del bordo; non null */
    private int colorEdge;
    /** Colore del riempimento; non null */
    private int colorFill;
    /** Colore del bordo quando il tasto è premuto; non null */
    private int colorEdgePress;
    /** Colore del riempimento quando il tasto è premuto; non null */
    private int colorFillPress;
    /** Stringa con il testo da mostrare; non null */
    private String text;
    /** Tipologia di tasto */
    private MacroKeyType type;


    /**
     * Istanzia un nuovo tasto con le proprietà di default
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
	 * @return ID del tasto; disponibile solo dopo il caricamento
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param ID del tasto; >= 0
	 * @throws IllegalArgumentException Se  {@code id} < 0
	 */
	void setId(int id) {
		if(id < 0) {
			throw new IllegalArgumentException("Parameter id must be >= 0");
		}
		this.id = id;
	}


	/**
     * @return Area (non modificabile) occupata dal tasto a schermo in millimetri; non null
     *   heigth e width sempre > 0
     */
    public RectF getArea() {
        return new RectF(area);
    }
    
    /**
     * @param s Schermo per il quale fornire la dimensione
     * @return Area occupata dal tasto a schermo in pixel; non null
     * @throws NullPointerException Se {@code s} è null
     */
    public RectF getAreaPixel(Screen s) {
    	Objects.requireNonNull(s);
    	return ScreenUtility.mmtopx(area, s);
    }

    /**
     * @return Forma del tasto; non null, modificabile
     */
    public KeyShape getShape() {
        return shape;
    }

    /**
     * @return Colore del bordo
     */
    public int getColorEdge() {
        return colorEdge;
    }

    /**
     * @return Colore di riempimento
     */
    public int getColorFill() {
        return colorFill;
    }

    /**
     * @return Testo del tasto; non null
     */
    public String getText() {
        return text;
    }

    /**
     * @return Colore del bordo quando il tasto è premuto
     */
    public int getColorEdgePress() {
        return colorEdgePress;
    }

    /**
     * @return Colore di riempimento quando il tasto è premuto
     */
    public int getColorFillPress() {
        return colorFillPress;
    }
    
    
    
    /**
     * @param c Colore di riempimento
     */
    public void setColorFill(int c) {
    	colorFill = c;
    }


	/**
	 * @param area Area a schermo del tasto in millimetri; non null
	 *  heigth e width sempre > 0
	 */
	public void setArea(RectF area) {
		Objects.requireNonNull(area);
		if(area.width() < 0 || area.height() < 0) {
			throw new IllegalArgumentException("The width and heigth must be >= 0");
		}
		
		this.area = new RectF(area);
	}


	/**
	 * @param shape Forma del tasto; non nullo
	 */
	public void setShape(KeyShape shape) {
		Objects.requireNonNull(shape);
		this.shape = shape;
	}


	/**
	 * @param colorEdge Colore del bordo
	 */
	public void setColorEdge(int colorEdge) {
		this.colorEdge = colorEdge;
	}


	/**
	 * @param colorEdgePress Colore del bordo quando il tasto è premuto
	 */
	public void setColorEdgePress(int colorEdgePress) {
		this.colorEdgePress = colorEdgePress;
	}


	/**
	 * @param colorFillPress Colore di riempimento quando il tasto è premuto
	 */
	public void setColorFillPress(int colorFillPress) {
		this.colorFillPress = colorFillPress;
	}


	/**
	 * @param text Testo del tasto
	 */
	public void setText(String text) {
		this.text = text == null ? "" : text;
	}
	
	
    /**
	 * @return Sequenza di tasti associata al tasto; non null, modificabile
	 */
	public LimitedKeySequence getKeySeq() {
		return macroSeq;
	}

	/**
	 * @param keySeq Sequenza di tasti da associare al tasto
	 * @throws NullPointerException Se {@code keySeq} è null
	 */
	public void setKeySeq(LimitedKeySequence keySeq) {
		Objects.requireNonNull(keySeq);
		this.macroSeq = keySeq;
	}

	/**
	 * @return Tipologia di comportamento del testo
	 */
	public MacroKeyType getType() {
		return type;
	}


	/**
	 * Imposta la tipologia del comportamento del tasto
	 * @param keystrokeOnUp Valore della tipologia
	 */
	public void setType(MacroKeyType type) {
		this.type = type;
	}

	@Override
    public Object clone() throws CloneNotSupportedException {
		return JavaUtil.utilDeepClone(this);
    }
	
	/**
	 * Equivalenza eseguita su OGNI attributo non solo sull'id
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof MacroKey)) {
			return false;
		} else {
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
