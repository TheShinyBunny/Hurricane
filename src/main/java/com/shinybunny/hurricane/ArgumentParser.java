package com.shinybunny.hurricane;

import com.shinybunny.hurricane.util.CommandParsingException;

@FunctionalInterface
public interface ArgumentParser<T> {

    T parse(InputReader reader) throws CommandParsingException;

}
