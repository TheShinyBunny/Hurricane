package com.shinybunny.hurricane.arguments;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.annotations.Greedy;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.SuggestionConsumer;

public class StringAdapter implements ArgumentAdapter<String> {
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public void init(Argument container, CommandRegisteringContext ctx) {

    }

    @Override
    public String parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        if (argument.hasFlag(Greedy.FLAG))
            return reader.readRest();
        else return reader.readWord();
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

    }
}
