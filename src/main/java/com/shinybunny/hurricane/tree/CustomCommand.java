package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;
import com.shinybunny.hurricane.util.CustomDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class CustomCommand extends CustomDataHolder implements CommandExecutor {

    protected String name;
    protected String description;
    protected Predicate<CommandSender> requirement;
    protected List<String> aliases = new ArrayList<>();
    protected List<Argument> arguments = new ArrayList<>();
    protected String noPermsMessage = "You have no permissions to use this command!";
    protected List<CommandHook> hooks = new ArrayList<>();

    public CustomCommand(String name) {
        this.name = name;
    }

    public void onRegister(CommandRegisteringContext ctx) {
        for (CommandHook h : hooks) {
            try {
                h.onRegistered(this,ctx);
            } catch (CommandRegisterFailedException e) {
                ctx.addError(e);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public CustomCommand description(String description) {
        this.description = description;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addHook(CommandHook hook) {
        hooks.add(hook);
    }

    public List<CommandHook> getHooks() {
        return hooks;
    }

    public <H extends CommandHook> H getHookByType(Class<H> type) {
        return (H) hooks.stream().filter(type::isInstance).findFirst().orElse(null);
    }

    public void addAlias(String alias) {
        this.aliases.add(alias);
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public void addArgument(Argument arg) {
        this.arguments.add(arg);
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public Predicate<CommandSender> getRequirement() {
        return requirement;
    }

    public boolean canUse(CommandSender sender) {
        return requirement == null || requirement.test(sender);
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

    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        ctx.setExecutor(this);
        if (reader.canRead()) {
            if (ctx.getApi().isAllowMultiSpaces()) {
                reader.skipSpace();
            } else {
                reader.expect(' ',"Expected a space to separate arguments!");
            }
        }
        for (Argument a : arguments) {
            a.parse(reader,ctx);
            if (reader.canRead() && a.isSyntax() && a.needsSpaceAfter()) {
                reader.expect(' ',"Expected a space to separate arguments!");
                if (ctx.getApi().isAllowMultiSpaces()) {
                    reader.skipSpace();
                }
            }
        }
    }

    public boolean nameMatches(String name) {
        for (String n : getNames()) {
            if (n.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public String getNoPermsMessage() {
        return noPermsMessage;
    }

    public void setNoPermsMessage(String noPermsMessage) {
        this.noPermsMessage = noPermsMessage;
    }

    public boolean hasSyntaxArgs() {
        return arguments.stream().anyMatch(Argument::isSyntax);
    }

    public boolean hasRequiredArgs() {
        return arguments.stream().anyMatch(a -> a.isRequired() && a.isSyntax());
    }
}
