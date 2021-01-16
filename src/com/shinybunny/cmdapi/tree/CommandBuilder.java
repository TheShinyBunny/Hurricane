package com.shinybunny.cmdapi.tree;

import com.shinybunny.cmdapi.Argument;
import com.shinybunny.cmdapi.CommandExecutor;
import com.shinybunny.cmdapi.CommandSender;

import java.util.function.Predicate;

public class CommandBuilder {

    private CommandNode node;

    public CommandBuilder(Argument argument) {
        this.node = argument;
    }

    public CommandBuilder(String name) {
        this.node = new CommandNode(name);
    }

    public static CommandBuilder literal(String name) {
        return new CommandBuilder(name);
    }

    public static CommandBuilder argument(String name, Class<?> type) {
        return new CommandBuilder(new Argument(name,type));
    }

    public CommandBuilder then(CommandBuilder builder) {
        this.node.addChild(builder.node);
        return this;
    }

    public CommandBuilder execute(CommandExecutor executor) {
        this.node.setExecutor(executor);
        return this;
    }

    public CommandBuilder require(Predicate<CommandSender> requirement) {
        this.node.setRequirement(requirement);
        return this;
    }

    public CommandNode build() {
        return node;
    }

}
