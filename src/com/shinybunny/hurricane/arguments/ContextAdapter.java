package com.shinybunny.hurricane.arguments;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.SuggestionConsumer;

public class ContextAdapter implements ArgumentAdapter<CommandExecutionContext> {
    @Override
    public void init(Argument container, CommandRegisteringContext ctx) {
        container.setSyntax(false);
    }

    @Override
    public CommandExecutionContext parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        return ctx;
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

    }

    @Override
    public Class<CommandExecutionContext> getType() {
        return CommandExecutionContext.class;
    }
}
