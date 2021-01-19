package com.shinybunny.hurricane.tree;

public class ParsedArgument {

    private Argument argument;
    private Object value;

    public ParsedArgument(Argument argument, Object value) {
        this.argument = argument;
        this.value = value;
    }

    @Override
    public String toString() {
        return argument + " = " + value;
    }

    public Argument getArgument() {
        return argument;
    }

    public Object getValue() {
        return value;
    }
}
