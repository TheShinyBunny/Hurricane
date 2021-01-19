package com.shinybunny.hurricane;

public interface CommandExecutor {

    CommandExecutor DEFAULT = ctx -> CommandResult.success();

    CommandResult<?> execute(CommandExecutionContext ctx);

}
