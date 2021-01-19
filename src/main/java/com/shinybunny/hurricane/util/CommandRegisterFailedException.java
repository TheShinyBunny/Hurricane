package com.shinybunny.hurricane.util;

public class CommandRegisterFailedException extends Exception {

    public CommandRegisterFailedException() {
    }

    public CommandRegisterFailedException(String message) {
        super(message);
    }

    public CommandRegisterFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandRegisterFailedException(Throwable cause) {
        super(cause);
    }
}
