package com.macrokeys;

/** Tipologia di macro key */
public enum MacroKeyType {
	/** La sequenza di tasti viene premuta e rilasciata quando il
	 * {@link MacroKey} viene rispettivamente premuto e
	 * rilasciato. Utile nei videogiochi
	 */
	Game,
	/** La sequenza di tasti viene premuta e rilasciata periodicamente
	 * quando il {@link MacroKey} è premuto. Utile per macro e
	 * programmi di scrittura
	 */
	Normal,
	/** 
	 * La sequenza di tasti viene premuto e rilasciato al rilascio del
	 * {@link MacroKey}.
	 * Utile per evitare ripetizioni inavvertite
	 */
	OnRelease
}
