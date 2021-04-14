package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.InputReader;
import com.shinybunny.hurricane.util.CommandParsingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreeCommand extends CustomCommand {

    private List<CustomCommand> subCommands;

    public TreeCommand(String name) {
        super(name);
        subCommands = new ArrayList<>();
    }

    @Override
    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        for (CustomCommand cmd : subCommands) {
            Optional<String> opt = reader.readOneOf(ctx.getApi().areLiteralsIgnoreCase(), cmd.getAliases().toArray(new String[0]));
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

    public List<CustomCommand> getSubCommands() {
        return subCommands;
    }
}
