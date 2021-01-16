package com.shinybunny.cmdapi.arguments;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.SuggestionProvider;

/**
 * An interface for defining A custom adapter for a command argument type.
 * <br/>
 * An argument adapter can modify the registration process of an {@link Argument}, change its settings,
 * and implement how it should read the input to create an instance of <code>&lt;T&gt;</code> as the value of the argument.
 * @param <T> The type of argument this adapter handles
 */
public interface ArgumentAdapter<T> extends SuggestionProvider {

    Class<T> getType();

    default boolean canApply(Argument arg, CommandRegisteringContext ctx) {
        return arg.instanceOf(getType());
    }

    void init(Argument container, CommandRegisteringContext ctx);

    T parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException;

    default T getDefault(CommandExecutionContext ctx) {
        return null;
    }

    default Priority getPriorityOn(ArgumentAdapter<?> other, CommandRegisteringContext ctx) {
        return Priority.DEFAULT;
    }

}
