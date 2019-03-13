package com.macrokeys;

import java.io.*;

/** 
 * Static class for Java utilities
 */
final class JavaUtil {
	
	private JavaUtil() {}
	
	/**
	 * Deep clone the given object
	 * @param obj Serializable Object to clone
	 * @return Cloned object; null if there is a problem when cloning
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
