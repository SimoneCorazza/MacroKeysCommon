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

import org.eclipse.jdt.annotation.NonNull;

import com.macrokeys.rendering.RectF;
import com.macrokeys.rendering.Renderer;
import com.macrokeys.screen.Screen;
import static com.macrokeys.MacroScreen.SwipeType;

/**
 * Manage a set of {@link MacroScreen}
 */
public final class MacroSetup implements Serializable {

    /**
	 * Serial for {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;

	/** Screens that compose this setup; never null */
    private List<MacroScreen> screens;

    /** Current selected screen; never null */
    private MacroScreen actualScreen;
    
    
    
    /**
     * @param l Screens to include
     * @throws IllegalArgumentException If {@code l} is empty
     */
    public MacroSetup(@NonNull List<MacroScreen> l) {
    	Objects.requireNonNull(l);
    	if(l.isEmpty()) {
    		throw new IllegalArgumentException("List null or empty");
    	}
    	
    	screens = new ArrayList<>(l);
    	actualScreen = screens.get(0);
    }
    
    
    /**
     * REnder the screen
     * @param r Rendering
     * @param s Screen to render
     * @param drawArea Where to render
     * @param keyPress Keys actually pressed
     */
    public void render(@NonNull Renderer r, @NonNull Screen s, @NonNull RectF drawArea,
    		@NonNull List<MacroKey> keyPress) {
    	assert actualScreen != null;
    	actualScreen.render(r, s, drawArea, keyPress);
    }
    
    
    
    /**
     * Loads a {@link MacroSetup}
     * <p>Per approfondire le eccezzioni contenute in {@link MSLoadException}
     * vedere {@link ObjectInputStream#readObject()}</p>
     * @param path File path to load
     * @return Loading result
     * @throws IOException If there is an IO error
     * @throws FileNotFoundException  If the file was not found
     * @throws SecurityException If the file cannot be read
     * @throws MSLoadException If there is an error on the loading of the file.
     * See {@link ObjectInputStream#readObject()} for more information
     */
    public static @NonNull MacroSetup load(@NonNull String path)
    		throws IOException, MSLoadException {
    	Objects.requireNonNull(path);
    	
    	FileInputStream fin = new FileInputStream(path);
    	MacroSetup s = load(fin);
    	fin.close();
    	return s;
    }
    
    /**
     * Loads a {@link MacroSetup}
     * <p>Per approfondire le eccezzioni contenute in {@link MSLoadException}
     * vedere {@link ObjectInputStream#readObject()}</p>
     * @param instr Input stream
     * @return Loading result
     * @throws IOException If there is an IO error
     * @throws FileNotFoundException  If the file was not found
     * @throws SecurityException If the file cannot be read
     * @throws MSLoadException If there is an error on the loading of the file.
     * See {@link ObjectInputStream#readObject()} for more information
     */
    public static @NonNull MacroSetup load(@NonNull InputStream instr)
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
     * Adapt the {@link MacroScreen}s in this setup in the given size.
     * <br/>
     * The keys have a minum size which cannot be exceded.
     * @param width Space on the X axis in millimiters
     * @param height Space on the Y axis in millimiters
     * @return Copy of this instance with the relative {@link MacroScreen}s
     * that fits in the given sizes
     * @throws IllegalArgumentException If one of the size if <= 0
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
     * Adapt the {@link MacroScreen} in this setup in the given size.
     * <br/>
     * The keys have a minum size which cannot be exceded.
     * @param width Space on the X axis in millimiters; > 0
     * @param height Space on the Y axis in millimiters; > 0
     * @param m Screen to fit
     */
    private void fitFor(float width, float height, @NonNull MacroScreen m) {
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
     * Save this as a file
     * @param path Path where to save the file
     * @throws IOException If an IO error occurs
     * @throws SecurityException If there are no write privileges
     */
    public void save(@NonNull String path) throws IOException {
    	Objects.requireNonNull(path);
    	
		FileOutputStream fout = new FileOutputStream(path);
		save(fout);
		fout.close();
    }
    
    /**
     * Save this on the given stream
     * @param outstr Stream
     * @throws IOException If an IO error occurs
     * @throws SecurityException If there are no write privileges
     */
    public void save(@NonNull OutputStream outstr) throws IOException {
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
     * Save this as annay of bytes
     * @return Data that rapresents this intance
     * @throws IOException If an IO error occurs
     */
    public byte[] saveAsByteArray() throws IOException {
    	ByteArrayOutputStream str = new ByteArrayOutputStream();
    	save(str);
    	return str.toByteArray();
    }
    
    

    /** 
     * Generates the ids of the {@link MacroKey} in the {@link MacroScreen}s
     */
    private void generateMacroKeysIDs() {
    	int counter = 0;
    	for(MacroScreen s : getMacroScreens()) {
    		for(MacroKey m : s.getKeys()) {
    			m.setId(counter++);
    		}
    	}
    }
    
    /**
     * Gets the {@link MacroKey} with the given id
     * @param id If of the {@link MacroKey} to get
     * @return Obtained key; null if not found
     * @throws IllegalArgumentException If {@code id} < 0
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
     * @return Screens of this setup; the list is read only
     */
    public @NonNull List<MacroScreen> getMacroScreens() {
        return Collections.unmodifiableList(screens);
    }

    /**
     * @return Selected {@link MacroScreen}
     */
    public @NonNull MacroScreen getActualScreen() {
    	assert actualScreen != null;
        return actualScreen;
    }

    /**
     * Change the current {@link MacroScreen} with the {@link MacroScreen}
     * associated with the given swipe
     * @param swipe Swipe associeted to the {@link MacroScreen} to select
     * @return True if the {@code swipe} is associated with a {@link MacroScreen} and was successful changesd
     */
    public boolean changeScreen(@NonNull SwipeType swipe) {
        for(MacroScreen m : screens) {
            SwipeType t = m.getSwipeType();
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

