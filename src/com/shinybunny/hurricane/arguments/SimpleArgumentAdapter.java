package com.shinybunny.hurricane.arguments;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.SuggestionConsumer;

public class SimpleArgumentAdapter<T> implements ArgumentAdapter<T> {
    private final Class<T> type;
    private final ArgumentParser<T> parser;

    public SimpleArgumentAdapter(Class<T> type, ArgumentParser<T> parser) {
        this.type = type;
        this.parser = parser;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void init(Argument container, CommandRegisteringContext ctx) {

    }

    @Override
    public T parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        return parser.parse(reader);
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

    }
}
