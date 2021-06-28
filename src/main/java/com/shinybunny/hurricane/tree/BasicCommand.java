package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandExecutor;
import com.shinybunny.hurricane.CommandHook;
import com.shinybunny.hurricane.CommandResult;
import com.shinybunny.hurricane.util.CommandFailedException;

import java.util.ArrayList;

public class BasicCommand extends CustomCommand {

    private CommandExecutor executor;

    public BasicCommand(String name, CommandExecutor executor) {
        super(name);
        this.executor = executor;
    }

    @Override
    public CommandResult<?> execute(CommandExecutionContext ctx) throws CommandFailedException {
        if (executor == null) return CommandResult.fail("No executable command found!");
        for (CommandHook h : hooks) {
            h.preExecute(this,new ArrayList<>(),ctx);
        }
        CommandResult<?> res;
        try {
            res = executor.execute(ctx);
        } catch (Exception e) {
            res = CommandResult.fail(e);
        }
        for (CommandHook h : hooks) {
            h.postExecute(this,res,ctx);
        }
        return res;
    }
}
