package com.shinybunny.cmdapi.tree;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.CustomDataHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class CommandNode extends CustomDataHolder {

    protected String name;
    private List<CommandNode> children = new ArrayList<>();
    private List<Argument> argChildren = new ArrayList<>();
    private Predicate<CommandSender> requirement;
    private CommandExecutor executor;

    public CommandNode(String name) {
        this.name = name;
    }

    public void addChild(CommandNode child) {
        this.children.add(child);
        if (child instanceof Argument) {
            argChildren.add((Argument) child);
        }
    }

    public List<CommandNode> getChildren() {
        return children;
    }

    public List<? extends CommandNode> getRelevantNodes(CommandAPI api, InputReader reader) {
        if (children.size() > argChildren.size()) {
            for (CommandNode child : children) {
                if (!(child instanceof Argument)) {
                    int pos = reader.getPos();
                    String text = reader.readWord();
                    reader.setPos(pos);
                    if (api.literalsEqual(text, child.name)) {
                        return Collections.singletonList(child);
                    }
                }
            }
        }
        return argChildren;
    }

    public boolean canUse(CommandSender sender) {
        return requirement == null || requirement.test(sender);
    }

    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        int start = reader.getPos();
        if (reader.canRead(name.length())) {
            int end = start + name.length();
            if (ctx.getApi().literalsEqual(reader.getString().substring(start,end),name)) {
                reader.setPos(end);
                if (!reader.canRead() || reader.peek() == ' ') {
                    return;
                }
                reader.setPos(start);
            }
        }
        throw new CommandParsingException("Expected literal '" + name + "'");
    }

    public String getName() {
        return name;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public void setRequirement(Predicate<CommandSender> requirement) {
        this.requirement = requirement;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }
}
