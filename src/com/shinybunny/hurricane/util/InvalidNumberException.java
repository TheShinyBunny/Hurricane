package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.SyntaxMarker;

public class InvalidNumberException extends CommandParsingException {
    public InvalidNumberException(String message, SyntaxMarker marker) {
        super(message,marker);
    }

    public InvalidNumberException(String message, Throwable cause, SyntaxMarker marker) {
        super(message, cause, marker);
    }
}
