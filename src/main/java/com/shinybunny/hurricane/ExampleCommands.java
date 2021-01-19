package com.shinybunny.hurricane;

import com.shinybunny.hurricane.annotations.*;
import com.shinybunny.hurricane.util.CommandFailedException;

import java.util.Random;

public class ExampleCommands {

    @Command
    @Feedback(success="Kicked user ${user.getName()} from the server for ${reason}!",fail="You have no permissions to use this command!")
    public static boolean kick(@Sender User sender, User user, @Default(string = "no apparent reason") @Greedy String reason) {
        return true;
    }

    @Command
    @Feedback(success = "You rolled a %d!",fail = "Minimum number cannot be greater than the maximum number!")
    public static int dice(@Range(min = 1) @Default(integer = 1) int min,
                           @Range(min = 2) @Default(integer = 6) int max) {
        if (min > max) {
            throw new CommandFailedException();
        }
        Random r = new Random();
        return r.nextInt(max - min - 1) + min;
    }

    @Command
    public static class Time {

        @Command
        @Feedback(success = "Set the time to ${ticks}")
        public static void set(int ticks) {
            System.out.println(">>> time is set to " + ticks);
        }

        @Command
        @Feedback(success = "Set the time to ${value.name()}")
        public static void set(TimeValue value) {
            set(value.value);
        }

        public enum TimeValue {
            DAY(1000),
            NIGHT(13000),
            MIDNIGHT(18000),
            NOON(6000);

            private int value;

            TimeValue(int i) {
                this.value = i;
            }
        }

    }

}
