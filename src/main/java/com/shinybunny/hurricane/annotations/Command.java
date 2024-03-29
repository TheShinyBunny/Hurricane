package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.Hurricane;
import com.shinybunny.hurricane.CommandContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used on any method that should be registered as a command inside of a command container.
 * Any other method without this annotation will not be interpreted as a command in the API.
 * <p>
 * This annotation can be used inside command containers to mark an inner class as a tree command (see {@link Hurricane#register(Object)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Command {

    /**
     * An alternative name for the command.
     * <p>
     * When absent or empty:
     * <ul>
     *     <li>If the target element is a method, will use the method's name.</li>
     *     <li>If the target element is a class, will use the class name without any 'command' prefix or suffix</li>
     * </ul>
     * @return A name for the target command.
     */
    String value() default "";

    /**
     * A description for the command. Can be used to provide command help.
     */
    String desc() default "";

}
