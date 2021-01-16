package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.Argument;

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
