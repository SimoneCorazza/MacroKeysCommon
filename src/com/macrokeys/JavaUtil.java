package com.macrokeys;

import java.io.*;

/** Classe statica contenente utility per java */
final class JavaUtil {
	
	private JavaUtil() {}
	
	/**
	 * Effettua una deep copy dell'oggetto indicato
	 * @param obj Oggetto da clonare
	 * @return Oggetto clonato; null se c'è un problema con la serializzazione
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
