package com.shinybunny.hurricane;

import com.shinybunny.hurricane.util.CommandFailedException;

public interface CommandExecutor {

    CommandExecutor DEFAULT = ctx -> CommandResult.success();

    CommandResult<?> execute(CommandExecutionContext ctx) throws CommandFailedException;

}
