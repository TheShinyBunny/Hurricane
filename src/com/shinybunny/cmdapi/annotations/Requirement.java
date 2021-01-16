package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.tree.MethodCommand;
import com.shinybunny.cmdapi.util.CommandFailedException;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Adapter(Requirement.Adapter.class)
public @interface Requirement {

    Class<? extends Callback<?>> value();

    String msg() default "You are not allowed to use this command";

    interface Callback<S extends CommandSender> {
        boolean check(S sender, CommandExecutionContext ctx);
    }

    class Adapter implements MethodAnnotationAdapter<Requirement> {

        @Override
        public void preExecute(MethodCommand cmd, Requirement annotation, List<Object> args, CommandExecutionContext ctx) {
            try {
                Callback cb = annotation.value().newInstance();
                if (!cb.check(ctx.getSender(),ctx)) {
                    throw new CommandFailedException(annotation.msg());
                }
            } catch (ReflectiveOperationException e) {
                throw new CommandFailedException(e);
            }
        }

        @Override
        public void postExecute(MethodCommand cmd, Requirement annotation, CommandResult<?> result, CommandExecutionContext ctx) {

        }

        @Override
        public Class<Requirement> getType() {
            return Requirement.class;
        }

        @Override
        public void init(Requirement instance, MethodCommand container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {

        }
    }

}
