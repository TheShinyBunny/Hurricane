package com.shinybunny.hurricane.arguments;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.tree.Argument;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.SuggestionConsumer;

public class BooleanAdapter implements ArgumentAdapter<Boolean> {
    @Override
    public void init(Argument container, CommandRegisteringContext ctx) {

    }

    @Override
    public Boolean parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        int start = reader.getPos();
        return "true".equals(reader.readOneOf("true","false").orElseThrow(()->new CommandParsingException("Expected 'true' or 'false'",reader.markerSince(start))));
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefault(CommandExecutionContext ctx) {
        return false;
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {
        suggestions.suggest("true");
        suggestions.suggest("false");
    }
}
