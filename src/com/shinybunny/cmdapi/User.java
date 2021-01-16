package com.shinybunny.cmdapi;

import com.shinybunny.cmdapi.arguments.ArgumentAdapter;
import com.shinybunny.cmdapi.util.CommandParsingException;
import com.shinybunny.cmdapi.util.SuggestionConsumer;

public class User implements CommandSender {

    private final String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public void sendMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void success(String msg) {
        System.out.println(msg);
    }

    public String getName() {
        return name;
    }

    @Override
    public void fail(String msg) {
        System.err.println(msg);
    }

    @Override
    public String toString() {
        return "User{" + name + '}';
    }

    public static class Adapter implements ArgumentAdapter<User> {

        @Override
        public Class<User> getType() {
            return User.class;
        }

        @Override
        public void init(Argument container, CommandRegisteringContext ctx) {

        }

        @Override
        public User parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
            return new User(reader.readWord());
        }

        @Override
        public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

        }
    }

}
