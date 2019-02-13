package com.macrokeys;

/** Eccezione dovuta ad un errore di caricamento di una {@link MacroSetup} */
public class MSLoadException extends Exception {

	/**
	 * Seriale per {@link Exception}
	 */
	private static final long serialVersionUID = 1L;

	
	public MSLoadException(Throwable inner) {
		super(inner);
	}
	
	public MSLoadException(String msg, Throwable inner) {
		super(msg, inner);
	}
}
