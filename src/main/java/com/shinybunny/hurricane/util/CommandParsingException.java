package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.SyntaxMarker;

public class CommandParsingException extends Exception {

    private SyntaxMarker marker;

    public CommandParsingException(String message, SyntaxMarker marker) {
        super(message);
        this.marker = marker;
    }

    public CommandParsingException(String message, Throwable cause, SyntaxMarker marker) {
        super(message, cause);
        this.marker = marker;
    }

    public CommandParsingException(String message) {
        super(message);
    }

    public CommandParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxMarker getMarker() {
        return marker;
    }

}
