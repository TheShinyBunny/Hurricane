package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a name and an optional description to a parameter argument
 */
@Adapter(Arg.Adapter.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {

    /**
     * A custom name for the argument
     */
    String value();

    /**
     * A description for the argument to use in command help.
     */
    String desc() default "";

    boolean optional() default false;

    class Adapter implements ParamAnnotationAdapter<Arg> {

        @Override
        public Class<Arg> getType() {
            return Arg.class;
        }

        @Override
        public void init(Arg instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {
            container.setName(instance.value());
            if (!instance.desc().isEmpty()) {
                container.setDescription(instance.desc());
            }
            if (instance.optional()) {
                container.setRequired(false);
            }
        }

        @Override
        public void validate(Object value, Arg annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {

        }
    }

}
