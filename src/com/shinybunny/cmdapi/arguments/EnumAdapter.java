package com.shinybunny.cmdapi.arguments;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.SuggestionConsumer;

import java.util.stream.Stream;

public class EnumAdapter implements ArgumentAdapter<Enum> {
    @Override
    public Class<Enum> getType() {
        return Enum.class;
    }

    @Override
    public void init(Argument arg, CommandRegisteringContext ctx) {

    }

    @Override
    public Enum<?> parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
        if (argument.getType().isEnum()) {
            Object[] consts = argument.getType().getEnumConstants();
            int start = reader.getPos();
            String name = reader.readWord();
            return Stream.of(consts).filter(c->c.toString().equalsIgnoreCase(name)).map(o->(Enum)o).findAny().orElseThrow(()->new CommandParsingException("Unknown " + argument.getName() + ": '" + name + "'",reader.markerSince(start)));
        }
        return null;
    }

    @Override
    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {
        if (arg.getType().isEnum()) {
            suggestions.suggestEnum((Class<? extends Enum<?>>) arg.getType());
        }
    }

}
