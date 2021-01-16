package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandExecutionContext;
import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.tree.ParameterArgument;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;

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

    class Adapter implements ParamAnnotationAdapter<Arg> {

        @Override
        public Object modify(Object value, Arg annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            return null;
        }

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
        }
    }

}
