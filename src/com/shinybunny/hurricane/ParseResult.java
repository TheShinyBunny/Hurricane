package com.shinybunny.hurricane;

import com.shinybunny.hurricane.tree.CommandNode;
import com.shinybunny.hurricane.util.CommandParsingException;

import java.util.Map;

public class ParseResult {

    private CommandExecutionContext context;
    private InputReader reader;
    private Map<CommandNode, CommandParsingException> exceptions;

    public ParseResult(CommandExecutionContext context, InputReader reader, Map<CommandNode, CommandParsingException> exceptions) {
        this.context = context;
        this.reader = reader;
        this.exceptions = exceptions;
    }

    public InputReader getReader() {
        return reader;
    }

    public Map<CommandNode, CommandParsingException> getExceptions() {
        return exceptions;
    }

    public CommandExecutionContext getContext() {
        return context;
    }
}
