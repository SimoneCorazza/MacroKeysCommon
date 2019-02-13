package com.macrokeys.comunication;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Objects;

import com.macrokeys.LimitedKeySequence;

/** Permette di gestire la pressione/rilascio dei tasti */
final class KeyPresser {
	
	private final Robot robot;
	
	/**
	 * @throws AWTException Se c'è un problema con l'inizializzazione di {@link Robot}
	 */
	public KeyPresser() throws AWTException {
		robot = new Robot();
	}
	
	
	/**
	 * Preme i tasti indicati
	 * @param s Sequenza di tasti da premere
	 * @throws NullPointerException Se {@code s} è null
	 */
	public synchronized void press(LimitedKeySequence s) {
		Objects.requireNonNull(s);
		
		for(int i : s.getKeys()) {
			robot.keyPress(i);
        }
	}
	
	/**
	 * Rilascia i tasti indicati
	 * @param s Sequenza di tasti da premere
	 * @throws NullPointerException Se {@code s} è null
	 */
	public synchronized void release(LimitedKeySequence s) {
		Objects.requireNonNull(s);
		
		for(int i : s.getKeys()) {
			robot.keyRelease(i);
        }
	}
}
