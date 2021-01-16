package com.shinybunny.cmdapi.arguments;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.annotations.Greedy;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.SuggestionConsumer;

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
