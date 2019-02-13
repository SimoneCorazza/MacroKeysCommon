package com.macrokeys;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.macrokeys.rendering.RectF;
import com.macrokeys.rendering.Renderer;
import com.macrokeys.screen.Screen;

/**
 * Classe che rappresenta un insieme di schermate con i relativi tasti
 */
public final class MacroSetup implements Serializable {

    /**
	 * Seriale per {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;

	/** Schermate delle macro */
    private List<MacroScreen> screens;

    /** Schermata attualmente in uso; mai null */
    private MacroScreen actualScreen;
    
    
    
    /**
     * @param l Lista contenente le schermate (viene copiata)
     * @throws IllegalArgumentException Se {@code l} è null o vuota
     */
    public MacroSetup(List<MacroScreen> l) {
    	if(l == null || l.isEmpty()) {
    		throw new IllegalAccessError("List null or empty");
    	}
    	
    	screens = new ArrayList<>(l);
    	actualScreen = screens.get(0);
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
    public void render(Renderer r, Screen s, RectF drawArea, List<MacroKey> keyPress) {
    	assert actualScreen != null;
    	actualScreen.render(r, s, drawArea, keyPress);
    }
    
    
    
    /**
     * Carica una {@link MacroSetup}
     * <p>Per approfondire le eccezzioni contenute in {@link MSLoadException}
     * vedere {@link ObjectInputStream#readObject()}</p>
     * @param path Percorso dal quale caricare il file
     * @return {@link MacroSetup} caricata; non null
     * @throws IOException Se c'è un errore di IO
     * @throws FileNotFoundException  Se il file non è stato trovato
     * @throws SecurityException Se il file indicato non può essere letto
     * @throws MSLoadException Se c'è un errore di caricamento del file (es. versione non corrisponde)
     * @throws NullPointerException Se {@code path} è null
     */
    public static MacroSetup load(String path)
    		throws IOException, MSLoadException {
    	Objects.requireNonNull(path);
    	
    	FileInputStream fin = new FileInputStream(path);
    	MacroSetup s = load(fin);
    	fin.close();
    	return s;
    }
    
    /**
     * Carica una {@link MacroSetup}
     * <p>Per approfondire le eccezzioni contenute in {@link MSLoadException}
     * vedere {@link ObjectInputStream#readObject()}</p>
     * @param path Percorso dal quale caricare il file
     * @return {@link MacroSetup} caricata; non null
     * @throws IOException Se c'è un errore di IO
     * @throws StreamCorruptedException Se l'header non è corretto
     * @throws SecurityException If untrusted subclass illegally overrides security-sensitive methods
     * @throws MSLoadException Se c'è un errore di caricamento del file (es. versione non corrisponde)
     * @throws NullPointerException Se {@code instr} è null
     */
    public static MacroSetup load(InputStream instr)
    		throws IOException, MSLoadException {
    	Objects.requireNonNull(instr);
    	
    	ObjectInputStream oos = new ObjectInputStream(instr);
		MacroSetup p;
		try {
			p = (MacroSetup)oos.readObject();
		} catch (ClassNotFoundException | InvalidClassException | 
				StreamCorruptedException | OptionalDataException e) {
			throw new MSLoadException(e);
		}
		return p;
    }
    
    /**
     * Permette di adattare le schermate presenti alla dimensione specificata.
     * In ogni caso i tasti hanno una dimensione minima oltre la quale
     * non è più possibile rimpicciolire.
     * @param width Spazio sull'asse X in millimetri disponibile; > 0
     * @param height Spazio sull'asse Y in millimetri disponibile; > 0
     * @return Clone di this che sta nello spazio indicato
     * @throws IllegalArgumentException se i vincoli dei parametri non sono rispettati
     */
    public MacroSetup fitFor(float width, float height) {
    	if(width < 0 || height < 0) {
    		throw new IllegalArgumentException("Parametri devono essere > 0");
    	}
    	
    	MacroSetup setup = null;
    	try {
    		setup = (MacroSetup) this.clone();
		} catch (CloneNotSupportedException e) {
			assert false : "Clone should be supported";
    		throw new RuntimeException("Clone should be supported", e);
		}
    	
    	
    	for(MacroScreen m : setup.getMacroScreens()) {
    		fitFor(width, height, m);
    	}
    	
    	return setup;
    }
    
    
    
    /**
     * Adatta la schermata indicata allo spazio indicato
     * @param width Spazio sull'asse X in millimetri disponibile; > 0
     * @param height Spazio sull'asse Y in millimetri disponibile; > 0
     * @param m Schermata da modificare
     */
    private void fitFor(float width, float height, MacroScreen m) {
    	assert width > 0 && height > 0;
    	assert m != null;
    	
    	//Trovo gli estremi della schermata
    	float maxX = 0, maxY = 0;
		for(MacroKey k : m.getKeys()) {
			maxX = Math.max(k.getArea().right, maxX);
			maxY = Math.max(k.getArea().bottom, maxY);
		}
		
		//Controllo se è necessario sistemare la schermata
		if(maxX > width || maxY > height) {
    		float max = Math.max(maxX, maxY);
    		float scale = Math.min(width / max, height / max);
    		
        	for(MacroKey k : m.getKeys()) {
        		RectF a = k.getArea();
        		a.left = a.left * scale;
        		a.top = a.top * scale;
        		a.right = a.right * scale;
        		a.bottom = a.bottom * scale;
        		k.setArea(a);
        	}
    	}
    }
    
    
    
    /**
     * Salva this come file
     * @param path Percorso dove salvare il file
     * @throws IOException Se c'è un errore di IO
     * @throws SecurityException Se non si hanno i privilegi di scrittura
     * @throws NullPointerException Se {@code path} è null
     */
    public void save(String path) throws IOException {
    	Objects.requireNonNull(path);
    	
		FileOutputStream fout = new FileOutputStream(path);
		save(fout);
		fout.close();
    }
    
    /**
     * Salva this nello stream
     * @param path Percorso dove salvare il file
     * @throws IOException Se c'è un errore di IO
     * @throws SecurityException If untrusted subclass illegally overrides security-sensitive methods
     * @throws NullPointerException Se {@code outstr} è null
     */
    public void save(OutputStream outstr) throws IOException {
    	Objects.requireNonNull(outstr);
    	
    	generateMacroKeysIDs();
		ObjectOutputStream oos = new ObjectOutputStream(outstr);
		try {
			oos.writeObject(this);
		} catch(InvalidClassException | NotSerializableException e) {
			assert false : "Exception must not happend: class structure wrong";
			e.printStackTrace();
		}
    }
    
    /**
     * Salva this come un'array di byte
     * @return Array di byte rappresentanti this
     * @throws IOException Se c'è un errore di IO
     */
    public byte[] saveAsByteArray() throws IOException {
    	ByteArrayOutputStream str = new ByteArrayOutputStream();
    	save(str);
    	return str.toByteArray();
    }
    
    

    /** Genera gli ID per i {@link MacroKey} contenuti in {@code this} */
    private void generateMacroKeysIDs() {
    	int counter = 0;
    	for(MacroScreen s : getMacroScreens()) {
    		for(MacroKey m : s.getKeys()) {
    			m.setId(counter++);
    		}
    	}
    }
    
    /**
     * Ottiene il macro key con l'id indicato
     * @param id ID relativo al {@link MacroKey} da ottenere
     * @return Tasto desiderato; null se non trovato
     * @throws IllegalArgumentException Se {@code id} < 0
     */
    public MacroKey macroKeyFromID(int id) {
    	if(id < 0) {
    		throw new IllegalArgumentException("Parameter id must be >= 0");
    	}
    	
    	for(MacroScreen s : getMacroScreens()) {
    		for(MacroKey m : s.getKeys()) {
    			if(m.getId() == id) {
    				return m;
    			}
    		}
    	}
    	
    	return null;
    }

    /**
     * @return Schermate delle macro
     */
    public List<MacroScreen> getMacroScreens() {
        return Collections.unmodifiableList(screens);
    }

    /**
     * @return Schermata attualmente in uso; non null
     */
    public MacroScreen getActualScreen() {
    	assert actualScreen != null;
        return actualScreen;
    }

    /**
     * Cambia la schermata attuale con quella avente lo swipe indicato
     * @param swipe Swipe associato alla schermata da cambiare
     * @return True: lo swipe è associato a una schermata
     */
    public boolean changeScreen(MacroScreen.SwipeType swipe) {
        for(MacroScreen m : screens) {
            MacroScreen.SwipeType t = m.getSwipeType();
            if(t.equals(swipe)) {
                actualScreen = m;
                return true;
            }
        }

        return false;
    }

    @Override
	public boolean equals(Object obj) {
    	if(obj == null || !(obj instanceof MacroSetup)) {
			return false;
		} else {
			List<MacroScreen> l1 =  getMacroScreens();
			 List<MacroScreen> l2 = ((MacroSetup)obj).getMacroScreens();
			 
			 if(l1.size() != l2.size()) {
				 return false;
			 }
			 
			 boolean r = true;
			 Iterator<MacroScreen> it1 = l1.iterator();
			 Iterator<MacroScreen> it2 = l2.iterator();
			 while(r && it1.hasNext()) {
				 r = it1.next().equals(it2.next());
			 }
			 
			 return r;
		}
	}
    
    @Override
    public int hashCode() {
    	int sum = 0;
    	for(MacroScreen s : getMacroScreens()) {
    		sum += s.hashCode();
    	}
    	return sum;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return JavaUtil.utilDeepClone(this);
    }
}

