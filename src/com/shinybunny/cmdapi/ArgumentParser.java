package com.shinybunny.cmdapi;

import com.shinybunny.cmdapi.util.CommandParsingException;

@FunctionalInterface
public interface ArgumentParser<T> {

    T parse(InputReader reader) throws CommandParsingException;

}
