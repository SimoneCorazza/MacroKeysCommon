package com.macrokeys;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Sequence of keys
 */
public final class LimitedKeySequence implements Serializable {

    /**
     * Serial for {@link Serializable}
     */
    private static final long serialVersionUID = 2L;

    /**
     * Maximum number of keys of all the sequences
     */
    public static final int MAX_KEYS = 30;

    /**
     * Sequence of keys
     */
    private final List<Integer> keys;

    /**
     * Sets an empty key sequence
     */
    public LimitedKeySequence() {
        keys = Collections.unmodifiableList(new ArrayList<Integer>());
    }

    /**
     * @param s Sequence to set
     * @throws IllegalArgumentException If the sequence is longer then the {@link #MAX_KEYS}
     */
    public LimitedKeySequence(Integer[] s) {
        if (s == null || s.length == 0) {
            keys = null;
        } else if (s.length <= MAX_KEYS) {
            keys = Collections.unmodifiableList(Arrays.asList(s));
        } else {
            throw new IllegalArgumentException("Too many keys: " + s.length + " (maximum " + MAX_KEYS + ")");
        }
    }

    /**
     * @return Sequence of keys
     */
    @NonNull
    public List<Integer> getKeys() {
        return keys;
    }

    /**
     * Indicates if the key is present in the sequence
     * @param key Key to find
     * @return True if the key is present, false otherwise
     */
    public boolean contains(Integer key) {
        return keys.contains(key);
    }

    @Override
    public String toString() {
        if (getKeys() == null || getKeys().size() == 0) {
            return "";
        } else {
            Iterator<Integer> it = getKeys().iterator();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < getKeys().size() - 1; i++) {
                sb.append(KeyEvent.getKeyText(it.next()));
                sb.append("+");
            }
            sb.append(KeyEvent.getKeyText(it.next()));
            return sb.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        } else {
            LimitedKeySequence l = (LimitedKeySequence) obj;
            if (keys == null && l.keys == null) {
                return true;
            // If one is null and the other is not
            } else if ((keys == null && l.keys != null) || (keys != null && l.keys == null)) {
                return false;
            } else {
                return l.keys.equals(keys);
            }
        }
    }

    @Override
    public int hashCode() {
        if (getKeys() == null) {
            return 0;
        } else {
            Integer sum = 0;
            for (Integer i : getKeys()) {
                sum = sum * 31 + i;
            }
            return sum;
        }
    }
}
