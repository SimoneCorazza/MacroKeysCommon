package com.macrokeys.screen;

/**
 * The exception thrown by the class {@link Screen}
 */
public class ScreenException extends Exception {

    private static final long serialVersionUID = 8536564861279807160L;

    public ScreenException() {
    }

    public ScreenException(String msg) {
        super(msg);
    }

    public ScreenException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
