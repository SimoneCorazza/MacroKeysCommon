package com.macrokeys;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Sequenza di tasti limitata superiormente */
public final class LimitedKeySequence implements Serializable {
	
	/**
	 * Seriale per {@link Serializable}
	 */
	private static final long serialVersionUID = 2L;

	/** Numero massimo di tasti possibili nella sequenza */
	public static final int MAX_KEYS = 30;
	
	/** Sequenza di tasti non modificabile mai null */
	private final List<Integer> keys;
	
	/**
	 * Imposta una sequenza vuota
	 */
	public LimitedKeySequence() {
		keys = Collections.unmodifiableList(new ArrayList<Integer>());
	}
	
	/**
	 * @param s Sequenza da settare
	 * @throws IllegalArgumentException Se la sequenza è più lunga del massimo
	 */
	public LimitedKeySequence(Integer[] s) {
		if(s == null || s.length == 0) {
			keys = null;
		} else if(s.length <= MAX_KEYS) {
			keys = Collections.unmodifiableList(Arrays.asList(s));
		} else {
			throw new IllegalArgumentException("Too many keys: " + s.length + " (maximum " + MAX_KEYS + ")");
		}
	}
	
	
	
	/**
	 * @return Sequenza di tasti non modificabile; mai null
	 */
	public List<Integer> getKeys() {
		return keys;
	}
	
	/**
	 * Indica se il tasto è presente nella sequenza
	 * @param key Tasto da cercare
	 * @return True se il tasto è presente
	 */
	public boolean contains(Integer key) {
		return keys.contains(key);
	}
	
	
	@Override
	public String toString() {
		if(getKeys() == null || getKeys().size() == 0) {
			return "";
		} else  {
			Iterator<Integer> it = getKeys().iterator();
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < getKeys().size() - 1; i++) {
				sb.append(KeyEvent.getKeyText(it.next()));
				sb.append("+");
			}
			sb.append(KeyEvent.getKeyText(it.next()));
			return sb.toString();
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !obj.getClass().equals(getClass())) {
			return false;
		} else {
			LimitedKeySequence l = (LimitedKeySequence)obj;
			if(keys == null && l.keys == null) {
				return true;
			//Caso uno sia null e l'altro no
			} else if((keys == null && l.keys != null) || (keys != null && l.keys == null)) {
				return false;
			} else {
				return l.keys.equals(keys);
			}
		}
	}
	
	@Override
	public int hashCode() {
		if(getKeys() == null) {
			return 0;
		} else {
			Integer sum = 0;
			for(Integer i : getKeys()) {
				sum = sum * 31 + i;
			}
			return sum;
		}
	}
	
}
