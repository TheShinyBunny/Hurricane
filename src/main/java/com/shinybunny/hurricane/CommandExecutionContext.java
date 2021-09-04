package com.shinybunny.hurricane;

import com.shinybunny.hurricane.tree.CustomCommand;
import com.shinybunny.hurricane.tree.ParsedArgument;
import com.shinybunny.hurricane.util.CustomDataHolder;

import java.util.*;

public class CommandExecutionContext extends CustomDataHolder {

    private Hurricane api;
    private final CommandSender sender;
    private InputReader reader;
    private Map<String, ParsedArgument> arguments;
    private CommandExecutor executor;

    public CommandExecutionContext(Hurricane api, CommandSender sender, InputReader reader) {
        this.api = api;
        this.sender = sender;
        this.reader = reader;
        this.arguments = new HashMap<>();
    }

    public CommandSender getSender() {
        return sender;
    }

    public InputReader getReader() {
        return reader;
    }

    public Hurricane getApi() {
        return api;
    }

    public List<ParsedArgument> getArguments() {
        return new ArrayList<>(arguments.values());
    }

    public Optional<ParsedArgument> getArg(String name) {
        return Optional.ofNullable(arguments.get(name));
    }

    public <T> T get(String name, Class<T> type) {
        return getArg(name)
                .filter(a->a.getArgument().typeExtends(type))
                .map(a->(T)a.getValue())
                .orElseThrow(()->new RuntimeException("No argument found named '" + name + "' of type " + type.getSimpleName()));
    }

    public CommandExecutionContext copy() {
        CommandExecutionContext ctx = new CommandExecutionContext(api, sender, reader);
        ctx.arguments = new HashMap<>(arguments);
        return ctx;
    }

    public void withArgument(String name, ParsedArgument arg) {
        arguments.put(name,arg);
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }
}
