package com.shinybunny.cmdapi;

public interface CommandExecutor {

    CommandExecutor DEFAULT = ctx -> CommandResult.success();

    CommandResult<?> execute(CommandExecutionContext ctx);

}
