package com.shinybunny.hurricane;

import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.util.ArrayList;
import java.util.List;

public class CommandRegisteringContext {

    private Hurricane api;
    private CommandContainer container;
    private String command;
    private boolean allowMoreArgs = true;
    private boolean cancelled;
    private List<CommandRegisterFailedException> errors = new ArrayList<>();

    public CommandRegisteringContext(Hurricane api, CommandContainer container, String command) {
        this.api = api;
        this.container = container;
        this.command = command;
    }

    public String getCommandName() {
        return command;
    }

    public Hurricane getApi() {
        return api;
    }

    public CommandContainer getContainer() {
        return container;
    }

    public void disallowMoreArguments() {
        allowMoreArgs = false;
    }

    public boolean allowsMoreArguments() {
        return allowMoreArgs;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void addError(String msg) {
        addError(new CommandRegisterFailedException(msg));
    }

    public void addError(CommandRegisterFailedException e) {
        errors.add(e);
    }

    public void printErrors() {
        if (!errors.isEmpty()) {
            System.err.println("Errors occurred when registering command '" + command + "':");
            for (Exception e : errors) {
                e.printStackTrace();
            }
        }
    }
}
