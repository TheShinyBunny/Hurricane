package com.shinybunny.cmdapi.arguments;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.annotations.AlwaysSender;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.SuggestionConsumer;

public class CommandSenderAdapter implements ArgumentAdapter<CommandSender> {
    @Override
    public void init(Argument container, CommandRegisteringContext ctx) {
        container.setSyntax(false);
    }

    @Override
    public boolean canApply(Argument arg, CommandRegisteringContext ctx) {
        return ArgumentAdapter.super.canApply(arg,ctx) && arg.getType().isAnnotationPresent(AlwaysSender.class);
    }

    @Override
    public CommandSender parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        return null;
    }

    @Override
    public Class<CommandSender> getType() {
        return CommandSender.class;
    }

    @Override
    public CommandSender getDefault(CommandExecutionContext ctx) {
        return ctx.getSender();
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

    }
}
