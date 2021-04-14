package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandSender;
import com.shinybunny.hurricane.InputReader;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.CustomDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class CustomCommand extends CustomDataHolder {

    protected String name;
    protected String description;
    protected Predicate<CommandSender> requirement;
    protected List<String> aliases = new ArrayList<>();
    protected String noPermsMessage = "You have no permissions to use this command!";

    public CustomCommand(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addAlias(String alias) {
        this.aliases.add(alias);
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public Predicate<CommandSender> getRequirement() {
        return requirement;
    }

    public boolean canUse(CommandSender sender) {
        return requirement.test(sender);
    }

    public void setRequirement(Predicate<CommandSender> requirement) {
        this.requirement = requirement;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>(aliases);
        names.add(name);
        return names;
    }

    public String getName() {
        return name;
    }

    public abstract void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException;

    public boolean nameMatches(String name) {
        return getNames().contains(name);
    }

    public String getNoPermsMessage() {
        return noPermsMessage;
    }

    public void setNoPermsMessage(String noPermsMessage) {
        this.noPermsMessage = noPermsMessage;
    }
}
