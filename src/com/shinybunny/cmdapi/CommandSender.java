package com.shinybunny.cmdapi;

public interface CommandSender {

    void sendMessage(String msg);

    void success(String msg);

    void fail(String msg);

    default void sendFeedback(boolean success, String msg) {
        if (success) {
            success(msg);
        } else {
            fail(msg);
        }
    }

    default Object getDelegate() {
        return this;
    }
}
