package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.CommandResult;
import com.shinybunny.hurricane.InputReader;
import com.shinybunny.hurricane.util.CommandFailedException;
import com.shinybunny.hurricane.util.CommandParsingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a command with multiple sub-commands. These sub commands may also be <code>TreeCommand</code>s
 */
public class TreeCommand extends CustomCommand {

    private List<CustomCommand> subCommands;

    public TreeCommand(String name) {
        super(name);
        subCommands = new ArrayList<>();
    }

    public TreeCommand description(String description) {
        return (TreeCommand) super.description(description);
    }

    @Override
    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        if (reader.canRead()) {
            if (ctx.getApi().isAllowMultiSpaces()) {
                reader.skipSpace();
            } else {
                reader.expect(' ',"Expected a space!");
            }
        }
        for (CustomCommand cmd : subCommands) {
            Optional<String> opt = reader.readOneOf(ctx.getApi().areLiteralsIgnoreCase(), cmd.getNames().toArray(new String[0]));
            if (reader.canRead() && reader.peek() != ' ') continue;
            if (opt.isPresent()) {
                if (cmd.canUse(ctx.getSender())) {
                    cmd.parse(reader, ctx);
                    return;
                } else {
                    throw new CommandParsingException(cmd.getNoPermsMessage());
                }
            }
        }
        throw new CommandParsingException("Invalid sub-command " + reader.readWord());
    }

    public void addSubCommand(CustomCommand sc) {
        this.subCommands.add(sc);
    }

    /**
     * Gets the list of the sub-commands this tree has.
     */
    public List<CustomCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public CommandResult<?> execute(CommandExecutionContext ctx) throws CommandFailedException {
        // TODO: 14/04/2021 implement default execution (when no sub command is provided)
        return null;
    }
}
