package com.shinybunny.hurricane;

import com.shinybunny.hurricane.tree.CustomCommand;
import com.shinybunny.hurricane.util.CommandFailedException;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.util.List;

public interface CommandHook {

    void onRegistered(CustomCommand cmd, CommandRegisteringContext ctx) throws CommandRegisterFailedException;

    void preExecute(CustomCommand cmd, List<Object> args, CommandExecutionContext ctx) throws CommandFailedException;

    void postExecute(CustomCommand cmd, CommandResult<?> result, CommandExecutionContext ctx);

}
