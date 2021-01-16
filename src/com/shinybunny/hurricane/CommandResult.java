package com.shinybunny.hurricane;

public class CommandResult<T> {

    private T result;
    private boolean success;
    private String message;

    public CommandResult(T result, boolean success, String message) {
        this.result = result;
        this.success = success;
        this.message = message == null ? "" : message;
    }

    @Override
    public String toString() {
        return "Result(" + (success ? "success" : "failure") + ": " + result + ", '" + message + "')";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }

    public static <T> CommandResult<T> result(boolean success, String msg) {
        return new CommandResult<>(null,success,msg);
    }

    public static <T> CommandResult<T> result(T result, boolean success, String msg) {
        return new CommandResult<>(result,success,msg);
    }

    public static <T> CommandResult<T> from(Object obj) {
        if (obj == null) return success();
        if (obj instanceof String) return result(true,obj.toString());
        if (obj instanceof Boolean) return ((boolean)obj) ? success() : fail();
        if (obj instanceof CommandResult) return (CommandResult)obj;
        return new CommandResult<>((T)obj,true,"");
    }

    public static <T> CommandResult<T> fail(Throwable e) {
        return (CommandResult<T>) result(e,false,e.getMessage());
    }

    public static <T> CommandResult<T> fail() {
        return result(false,"");
    }

    public static <T> CommandResult<T> fail(String msg) {
        return result(false,msg);
    }

    public static <T> CommandResult<T> success() {
        return result(true,"");
    }

    public static <T> CommandResult<T> success(String msg) {
        return result(true,msg);
    }

    public boolean isSuccessful() {
        return success;
    }
}
