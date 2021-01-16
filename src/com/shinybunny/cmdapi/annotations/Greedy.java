package com.shinybunny.cmdapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a String argument as "greedy", making the argument read the rest of the input and use it as the value.
 * Arguments after an argument annotated with Greedy shouldn't be part of the syntax, as they'll be ignored and set to their default value.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@DummyAdapter(flag = Greedy.FLAG)
public @interface Greedy {

    String FLAG = "defaults.greedy";

}
