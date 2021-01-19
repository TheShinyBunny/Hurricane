package com.shinybunny.hurricane.util;

public class CommandFailedException extends RuntimeException {

    public CommandFailedException() {
    }

    public CommandFailedException(String message) {
        super(message);
    }

    public CommandFailedException(Throwable cause) {
        super(cause);
    }
}
