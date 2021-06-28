package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.annotations.AnnotationAdapterContainer;
import com.shinybunny.hurricane.annotations.Command;
import com.shinybunny.hurricane.annotations.MethodAnnotationAdapter;
import com.shinybunny.hurricane.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class representing a registered command that was interpreted from a method declaration.
 * <br/>
 * Method commands are created when registering a holder class through {@link Hurricane#register(Object)}.
 * Each method inside the provided class, if it has the {@link Command} annotation, is translated to a new instance of a <code>MethodCommand</code>.
 * The method's parameters are all translated to {@link ParameterArgument}s, and a command structure is automatically built from that.
 * <br/>
 * Method commands can be annotated with custom annotations, to modify the registration and execution processes of the command.
 * To define a custom handler for a custom method annotation, implement the {@link MethodAnnotationAdapter}.
 */
public class MethodCommand extends CustomCommand implements AnnotationAdapterContainer<MethodAnnotationAdapter>, CommandExecutor {

    private Method method;
    private CommandContainer container;

    public MethodCommand(CommandRegisteringContext ctx, Method method, CommandContainer container) {
        super(Utils.getName(method));
        this.method = method;
        this.noPermsMessage = ctx.getApi().getDefaultNoPermsMessage();
        this.container = container;
        this.description = method.getAnnotation(Command.class).desc();
        List<MethodAnnotationAdapter> adapters = resolveAnnotationAdapters(ctx);
        for (MethodAnnotationAdapter a : adapters) {
            addHook(a.convertToHook(getAnnotation(a.getType())));
        }
    }

    @Override
    public MethodAnnotationAdapter dummyAdapter(Class<? extends Annotation> annotationType) {
        return new MethodAnnotationAdapter() {

            @Override
            public void preExecute(MethodCommand cmd, Annotation annotation, List args, CommandExecutionContext ctx) {

            }

            @Override
            public void postExecute(MethodCommand cmd, Annotation annotation, CommandResult result, CommandExecutionContext ctx) {

            }

            @Override
            public Class getType() {
                return annotationType;
            }

            @Override
            public void init(Annotation instance, Object container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {

            }

        };
    }

    @Override
    public MethodAnnotationAdapter getAdapterFor(CommandRegisteringContext ctx, Class<? extends Annotation> annotationType) {
        return ctx.getApi().getMethodAnnotationAdapter(annotationType);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return method;
    }

    @Override
    public CustomDataHolder getDataHolder() {
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public CommandContainer getContainer() {
        return container;
    }

    @Override
    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        super.parse(reader, ctx);
    }

    @Override
    public CommandResult<Object> execute(CommandExecutionContext ctx) throws CommandFailedException {
        ctx.getApi().log("performing execution of method command " + this);
        Object instance = Modifier.isStatic(method.getModifiers()) ? null : container.getInstance(ctx);
        Object[] args = new Object[method.getParameterCount()];
        ctx.getApi().log("\tgathering arguments...");
        for (ParsedArgument arg : ctx.getArguments()) {
            Argument a = arg.getArgument();
            if (a instanceof ParameterArgument) {
                ctx.getApi().log("\t\t[" + a.name + "] = " + arg.getValue());
                args[((ParameterArgument) a).getIndex()] = arg.getValue();
            }
        }
        ctx.getApi().log("\trunning preExecute...");
        for (CommandHook h : hooks) {
            try {
                ctx.getApi().log("\t\t" + h);
                List<Object> tempArgs = new ArrayList<>(Arrays.asList(args));
                h.preExecute(this, tempArgs, ctx);
                args = tempArgs.toArray();
            } catch (Exception e) {
                return CommandResult.fail(e);
            }
        }
        Object ret;
        try {
            ctx.getApi().log("\tinvoking method " + method);
            ret = method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            ret = CommandResult.fail(e.getCause());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new CommandFailedException(e);
        }
        CommandResult<Object> result = CommandResult.from(ret);
        ctx.getApi().log("\trunning postExecute...");
        for (CommandHook h : hooks) {
            ctx.getApi().log("\t\t" + h);
            h.postExecute(this,result,ctx);
        }
        ctx.getApi().log("\tdone!");
        return result;
    }

    public boolean hasAnnotation(Class<? extends Annotation> a) {
        return method.isAnnotationPresent(a);
    }

    public <A extends Annotation> A getAnnotation(Class<A> a) {
        return method.getAnnotation(a);
    }

    @Override
    public String toString() {
        return "MethodCommand{" +
                "name='" + name + '\'' +
                ", method=" + method +
                "}";
    }


}
