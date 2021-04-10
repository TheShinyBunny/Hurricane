package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.tree.MethodCommand;
import com.shinybunny.hurricane.util.CommandFailedException;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

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

    interface Callback<S extends CommandSender> {
        boolean check(S sender);
    }

    class Adapter implements MethodAnnotationAdapter<Requirement> {

        @Override
        public void preExecute(MethodCommand cmd, Requirement annotation, List<Object> args, CommandExecutionContext ctx) throws CommandFailedException {

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
            container.setRequirement(sender->{
                try {
                    Callback cb = instance.value().newInstance();
                    if (cb.check(sender)) {
                        return true;
                    }
                } catch (ReflectiveOperationException ignored) {

                }
                return false;
            });
        }
    }

}
