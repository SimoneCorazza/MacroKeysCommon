package com.macrokeys;

import com.macrokeys.rendering.*;
import com.macrokeys.screen.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Classe che memorizza una schermata di una MacroSetup con i relativi tasti
 */
public final class MacroScreen implements Cloneable, Serializable {

    /**
	 * Seriale per {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;


    /** Lista dei tasti presenti in questa schermata; non null */
    private List<MacroKey> keys;
    /** Colore del background; non null */
    private int colorBackground;
    /** Testo di background; non null */
    private String backgroundText;
    /** Tipologia di swipe che richiama questa schermata ; non null */
    private SwipeType swipeType;
    /** Orientazione della schermata */
    private Orientation orientation;

    /**
     * Crea una macro Screen vuota
     */
    public MacroScreen() { 
    	keys = new ArrayList<>();
    	colorBackground = Color.WHITE;
    	backgroundText = "";
    	swipeType = SwipeType.Finger2_Up;
    	orientation = Orientation.Horizontal;
    }
    
    /**
     * Renderizza lo screen a video
     * @param r Oggetto di rendering
     * @param s Schermo sul quale renderizzare
     * @param drawArea Area di rendering
     * @param keyPress Tasti attualmente premuti
     * @throws NullPointerException Se {@code r} o {@code s} o {@code drawArea} o
     * {@code keyPress} è null
     */
    public void render(Renderer r, Screen s, RectF drawArea,
    		List<MacroKey> keyPress) {
    	Objects.requireNonNull(r);
    	Objects.requireNonNull(s);
    	Objects.requireNonNull(drawArea);
    	Objects.requireNonNull(keyPress);
    	
        r.setTextSize(50);
        r.setAntiAlias(true);
        r.setTextAllign(TextAllign.Center);

        //Sfondo e testo per il background
        r.setPaintStyle(PaintStyle.Fill);
        r.setColor(getBackgroundColor());
        r.rect(drawArea);
        drawStringRect(r, getBackgroundText(), Color.BLACK, drawArea);
        
        for(MacroKey k : getKeys()) {
        	
        	int border, fill; //Colori del tasto
            //Controllo se è il tasto premuto (confronto tra istanze (puntatori))
            if(contains(k, keyPress)) {
                border = k.getColorEdgePress();
                fill = k.getColorFillPress();
            } else {
                border = k.getColorEdge();
                fill = k.getColorFill();
            }
            //Area del bottone in pixel
            RectF pix = k.getAreaPixel(s);

            if(k.getShape().getType().equals(KeyShape.Type.Ellipse)) {
                r.setPaintStyle(PaintStyle.Fill);
                r.setColor(fill);
                r.ellipse(pix);
                r.setPaintStyle(PaintStyle.Stroke);
                r.setColor(border);
                r.ellipse(pix);
            } else { //Caso non previto o caso rettangolo disegno come rettangolo
            	r.setPaintStyle(PaintStyle.Fill);
                r.setColor(fill);
                r.rect(pix);
                r.setPaintStyle(PaintStyle.Stroke);
                r.setColor(border);
                r.rect(pix);
            }
            drawStringRect(r, k.getText(), Color.BLACK, pix);
        }
    }

    
    /**
     * @param m
     * @param l
     * @return True se il tasto {@code m} è contenuto nella lista {@code l},
     * False altriemnti
     */
    private static boolean contains(MacroKey m, List<MacroKey> l) {
    	assert m != null && l != null;
    	
    	for(MacroKey mm : l) {
    		if(m == mm) {
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * Renderizza la scritta all'interno dell'area indicata, se possibile
     * @param r Componente di rendering, tramite la quale renderizzare la stringa
     * @param s Stringa da renderizzare
     * @param color Colore della stringa
     * @param area Area del rettangolo dove posizionarla
     */
    private static void drawStringRect(Renderer r, String s, int color, RectF area) {
    	assert r != null && s != null && area != null;
    	
    	r.setColor(color);
		r.text(s, area);
	}


    /**
     * @return Lista (modificabile) di tasti associati alla schermata; non null
     */
    public List<MacroKey> getKeys() {
    	assert keys != null;
        return keys;
    }

    /**
     * @return Colore del background; non null
     */
    public int getBackgroundColor() {
        return colorBackground;
    }

    /**
     * @return Testo di background; non null
     */
    public String getBackgroundText() {
        return backgroundText;
    }

    /**
     * @return Metodo di richiamo dello screen mediante gesti
     */
    public SwipeType getSwipeType() {
        return swipeType;
    }

    /**
     * @return Orientazione della schermata
     */
    public Orientation getOrientation() {
        return orientation;
    }

	/**
	 * @param swipeType Swipe che richiama questa schermata
	 */
	public void setSwipeType(SwipeType swipeType) {
		this.swipeType = swipeType;
	}

	/**
	 * @param colorBackground Colore di background
	 */
	public void setBackgroundColor(int colorBackground) {
		this.colorBackground = colorBackground;
	}

	/**
	 * @param backgroundText Testo di background
	 */
	public void setBackgroundText(String backgroundText) {
		this.backgroundText = backgroundText;
	}

	/**
	 * @param orientation Orientazione della schermata
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	/**
     * Rileva se alle coordinate fornite si trova un tasto
     * @param x Coordinata X della pressione in pixel
     * @param y Coordinata y della pressione in pixel
     * @param s Schermo correntemente utilizzato
     * @return Tasto presente; null se nessun tasto è presente
     * @throws NullPointerException Se {@code s} è null
     */
	public MacroKey keyAt(float x, float y, Screen s) {
		Objects.requireNonNull(s);
		
		//Scandisco al contrario per coerenza con il rendering
		//(se vengono renderizzate dalla testa alla coda -> l'utente vede per
		//prima le cose renderizzate per ultime)
		ListIterator<MacroKey> it = getKeys().listIterator(getKeys().size());
        while (it.hasPrevious()) {
        	MacroKey k = it.previous();
            if(keyIntersect(k, s, x, y)) {
                return k;
            }
        }
        return null;
	}
	
	/**
	 * Indica se il tasto indicato è stato intersecato dal punto
	 * @param m Tasto da testare; non null
	 * @param s Oggetto schermo da cui ricavare le informazioni
	 * sulla reale dimensione del tasto; non null
	 * @param x Coordinata X del punto da testare
	 * @param y Coordinata Y del punto da testare
	 * @return Risultato dell'intersezione: true avvennuta
	 */
	private static boolean keyIntersect(MacroKey m, Screen s, float x, float y) {
		assert m != null && s != null;
		
		final RectF r = m.getAreaPixel(s);
		
		switch(m.getShape().getType()) {
			case Rectangle:	return r.contains(x, y);
			case Ellipse:	return pointInEllipse(
					r.centerX(), r.centerY(), 
					r.width() / 2, r.height() / 2, //Dimezzo il diametro
					x, y
					);
				
			default:
				assert false : "Unnkown case";
				return false;
		}
	}
	
	/**
	 * Testa l'intersezione tra un ellisse (aventi glia assi concordanti con gli assi cartesiani) e un punto
	 * @param cx Coordinata X del centro dell'ellisse
	 * @param cy Coordinata Y del centro dell'ellisse
	 * @param rx Raggio sull'asse X dell'ellisse
	 * @param ry Raggio sull'asse Y dell'ellisse
	 * @param x Coordinata X del punto da testare
	 * @param y Coordinata X del punto da testare
	 * @return Risultato del test: true intersezione avvenuta
	 * @see {@linkplain http://math.stackexchange.com/questions/76457/check-if-a-point-is-within-an-ellipse}
	 */
	private static boolean pointInEllipse(float cx, float cy, float rx, float ry, float x, float y) {
		return ((x - cx) * (x - cx)) / (rx * rx) + ((y - cy) * (y - cy)) / (ry * ry) <= 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof MacroScreen)) {
			return false;
		} else {
			MacroScreen s = ((MacroScreen)obj);
			List<MacroKey> l1 = getKeys();
			List<MacroKey> l2 = s.getKeys();

			if (l1.size() != l2.size() || 
					getBackgroundColor() != s.getBackgroundColor() ||
					!getBackgroundText().equals(s.getBackgroundText()) ||
					!getOrientation().equals(s.getOrientation()) ||
					!getSwipeType().equals(s.getSwipeType())) {
				return false;
			}

			boolean r = true;
			Iterator<MacroKey> it1 = l1.iterator();
			Iterator<MacroKey> it2 = l2.iterator();
			while (r && it1.hasNext()) {
				r = it1.next().equals(it2.next());
			}

			return r;
		}
	}
	
	@Override
	public int hashCode() {
		int sum = 0;
		for(MacroKey k : getKeys()) {
			sum += k.hashCode();
		}
		sum += getBackgroundColor();
		sum += getBackgroundText().hashCode();
		sum += getOrientation().hashCode();
		sum += getSwipeType().hashCode();
		
		return sum;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return JavaUtil.utilDeepClone(this);
	}
	

    /** Gesto che richiama la schermata */
    public enum SwipeType {
        Finger2_Up,
        Finger2_Right,
        Finger2_Down,
        Finger2_Left,

        Finger3_Up,
        Finger3_Right,
        Finger3_Down,
        Finger3_Left
    }

    /** Orientamento della schermata */
    public enum Orientation {
        /** Verticale */
        Vertical,
        /** Orizzontale */
        Horizontal,
        /** In base all'accelerometro */
        Rotate
    }
}

