package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandExecutionContext;
import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.tree.ParameterArgument;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Adapter(Sender.Adapter.class)
public @interface Sender {

    class Adapter implements ParamAnnotationAdapter<Sender> {

        @Override
        public Object modify(Object value, Sender annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            if (argument.getType().isInstance(ctx.getSender())) {
                return ctx.getSender();
            }
            if (argument.getType().isInstance(ctx.getSender().getDelegate())) {
                return ctx.getSender().getDelegate();
            }
            throw new Exception("You cannot use this command!");
        }

        @Override
        public Class<Sender> getType() {
            return Sender.class;
        }

        @Override
        public void init(Sender instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {
            container.setSyntax(false);
        }
    }

}
