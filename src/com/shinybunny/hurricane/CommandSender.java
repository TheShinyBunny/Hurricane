package com.shinybunny.hurricane;

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

    CommandSender CONSOLE = new CommandSender() {
        @Override
        public void sendMessage(String msg) {
            System.out.println(msg);
        }

        @Override
        public void success(String msg) {
            System.out.println(msg);
        }

        @Override
        public void fail(String msg) {
            System.err.println(msg);
        }
    };
}
