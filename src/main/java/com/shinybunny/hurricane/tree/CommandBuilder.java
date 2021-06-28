package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.CommandExecutor;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public abstract class CommandBuilder<T extends CustomCommand> {

    private final String name;
    private String description;
    private Predicate<CommandSender> requirement;

    public CommandBuilder(String name) {
        this.name = name;
    }

    public static BasicBuilder create(String name) {
        return new BasicBuilder(name);
    }

    public static TreeBuilder tree(String name) {
        return new TreeBuilder(name);
    }

    public CommandBuilder<T> description(String desc) {
        this.description = desc;
        return this;
    }

    public CommandBuilder<T> requires(Predicate<CommandSender> requirement) {
        this.requirement = requirement;
        return this;
    }

    public String getName() {
        return name;
    }

    public T build(CommandRegisteringContext ctx) {
        T cmd = init(name,ctx);
        cmd.description(description);
        cmd.setRequirement(requirement);
        return cmd;
    }

    protected abstract T init(String name, CommandRegisteringContext ctx);

    public static class TreeBuilder extends CommandBuilder<TreeCommand> {

        private List<CommandBuilder> subCommands = new ArrayList<>();

        public TreeBuilder(String name) {
            super(name);
        }

        @Override
        protected TreeCommand init(String name, CommandRegisteringContext ctx) {
            TreeCommand cmd = new TreeCommand(name);
            for (CommandBuilder b : subCommands) {
                cmd.addSubCommand(b.build(ctx));
            }
            return cmd;
        }

        public TreeBuilder sub(CommandBuilder builder) {
            subCommands.add(builder);
            return this;
        }

        @Override
        public TreeBuilder description(String desc) {
            return (TreeBuilder) super.description(desc);
        }

        @Override
        public TreeBuilder requires(Predicate<CommandSender> requirement) {
            return (TreeBuilder) super.requires(requirement);
        }

    }

    public static class BasicBuilder extends CommandBuilder<BasicCommand> {

        private final List<Argument> args = new ArrayList<>();
        private CommandExecutor executor;

        public BasicBuilder(String name) {
            super(name);
        }

        @Override
        public BasicBuilder description(String desc) {
            return (BasicBuilder) super.description(desc);
        }

        @Override
        public BasicBuilder requires(Predicate<CommandSender> requirement) {
            return (BasicBuilder) super.requires(requirement);
        }

        @Override
        protected BasicCommand init(String name, CommandRegisteringContext ctx) {
            BasicCommand cmd = new BasicCommand(name,executor);
            for (Argument arg : args) {
                cmd.addArgument(arg);
                arg.postInit(ctx);
            }
            return cmd;
        }

        public BasicBuilder args(Argument... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        public BasicBuilder execute(CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

    }

}
