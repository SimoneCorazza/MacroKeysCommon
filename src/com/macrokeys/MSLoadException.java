package com.macrokeys;

/** Exception when an error occur while loading a {@link MacroSetup} */
public class MSLoadException extends Exception {

	/**
	 * Serial for {@link Exception}
	 */
	private static final long serialVersionUID = 1L;

	
	public MSLoadException(Throwable inner) {
		super(inner);
	}
	
	public MSLoadException(String msg, Throwable inner) {
		super(msg, inner);
	}
}
