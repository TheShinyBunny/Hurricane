package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.tree.Argument;
import com.shinybunny.hurricane.Hurricane;
import com.shinybunny.hurricane.CommandSender;
import com.shinybunny.hurricane.InputReader;
import com.shinybunny.hurricane.ParseResult;

/**
 * A custom definition of a suggestion list provider. Suggestions can be used by programs with any kind of command completion/suggestion system.
 */
public interface SuggestionProvider {

    /**
     * Suggests values to the {@link SuggestionConsumer} parameter.
     * @param reader The input reader of the entire command, with {@link InputReader#getPos() it's pos} set to the position of the cursor.
     * @param suggestions The suggestion consumer that takes all the possible suggestions.
     * @param sender The source that requested to list suggestions
     * @param arg The argument that used this provider
     */
    void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg);

}
