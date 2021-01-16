package com.shinybunny.cmdapi.util;

import com.shinybunny.cmdapi.Argument;
import com.shinybunny.cmdapi.CommandAPI;
import com.shinybunny.cmdapi.CommandSender;
import com.shinybunny.cmdapi.InputReader;
import com.shinybunny.cmdapi.ParseResult;

/**
 * A custom definition of a suggestion list provider. Suggestions can be used by programs with any kind of command completion/suggestion system.
 */
public interface SuggestionProvider {

    /**
     * Suggests values to the {@link SuggestionConsumer} parameter. Called every time this provider is activated by calling {@link CommandAPI#getSuggestions(ParseResult, int)}
     * @param reader The input reader of the entire command, with {@link InputReader#getPos() it's pos} set to the position of the cursor.
     * @param suggestions The suggestion consumer that takes all the possible suggestions.
     * @param sender The source that requested to list suggestions
     * @param arg The argument that used this provider
     */
    void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg);

}
