package com.macrokeys;

import java.io.*;

/**
 * Static class for Java utilities
 */
final class JavaUtil {

    private JavaUtil() {
    }

    /**
     * Deep clone the given object
     * @param obj the Serializable Object to clone
     * @return Cloned object
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T utilDeepClone(T obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (ClassCastException e) {
            assert false : "Serialized object must be the same when loaded";
            return null;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
