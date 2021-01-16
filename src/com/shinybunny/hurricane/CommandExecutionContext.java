package com.shinybunny.hurricane;

import com.shinybunny.hurricane.tree.ParsedArgument;
import com.shinybunny.hurricane.util.CustomDataHolder;

import java.util.*;

public class CommandExecutionContext extends CustomDataHolder {

    private CommandAPI api;
    private final CommandSender sender;
    private InputReader reader;
    private Map<String, ParsedArgument> arguments;
    private CommandExecutor executor;

    public CommandExecutionContext(CommandAPI api, CommandSender sender, InputReader reader, CommandExecutor executor) {
        this.api = api;
        this.sender = sender;
        this.reader = reader;
        this.executor = executor;
        this.arguments = new HashMap<>();
    }

    public CommandSender getSender() {
        return sender;
    }

    public InputReader getReader() {
        return reader;
    }

    public CommandAPI getApi() {
        return api;
    }

    public CommandExecutionContext copyFor(CommandSender sender) {
        if (sender == this.sender) return this;
        return new CommandExecutionContext(api,sender,reader, executor);
    }

    public Collection<ParsedArgument> getArguments() {
        return arguments.values();
    }

    public Optional<ParsedArgument> getArg(String name) {
        return Optional.ofNullable(arguments.get(name));
    }

    public CommandExecutionContext copy() {
        CommandExecutionContext ctx = new CommandExecutionContext(api, sender, reader, executor);
        ctx.arguments = new HashMap<>(arguments);
        return ctx;
    }

    public void withArgument(String name, ParsedArgument arg) {
        arguments.put(name,arg);
    }

    public void withExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }
}
