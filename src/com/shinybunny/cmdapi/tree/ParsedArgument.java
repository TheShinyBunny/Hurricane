package com.shinybunny.cmdapi.tree;

import com.shinybunny.cmdapi.Argument;

public class ParsedArgument {

    private Argument argument;
    private Object value;

    public ParsedArgument(Argument argument, Object value) {
        this.argument = argument;
        this.value = value;
    }

    public Argument getArgument() {
        return argument;
    }

    public Object getValue() {
        return value;
    }
}
