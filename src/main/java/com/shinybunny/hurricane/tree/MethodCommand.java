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
public class MethodCommand extends CommandNode implements AnnotationAdapterContainer<MethodAnnotationAdapter>, CommandExecutor {

    private Method method;
    private CommandContainer container;
    private List<MethodAnnotationAdapter> annotationAdapters;
    private List<ParameterArgument> syntaxlessArgs = new ArrayList<>();

    public MethodCommand(CommandRegisteringContext ctx, Method method, CommandContainer container) {
        super(Utils.getName(method));
        this.method = method;
        this.container = container;
        this.description = method.getAnnotation(Command.class).desc();
        this.annotationAdapters = resolveAnnotationAdapters(ctx);
        for (MethodAnnotationAdapter a : annotationAdapters) {
            try {
                a.init(method.getAnnotation(a.getType()),this,ctx);
            } catch (CommandRegisterFailedException e) {
                ctx.addError(e);
            }
        }
    }

    public void postInit(CommandRegisteringContext ctx) {
        for (MethodAnnotationAdapter a : annotationAdapters) {
            try {
                a.postInit(method.getAnnotation(a.getType()),this,ctx);
            } catch (CommandRegisterFailedException e) {
                ctx.addError(e);
            }
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
    public CommandResult<Object> execute(CommandExecutionContext ctx) {
        System.out.println("performing execution of method command " + this);
        Object instance = Modifier.isStatic(method.getModifiers()) ? null : container.getInstance(ctx);
        Object[] args = new Object[method.getParameterCount()];
        System.out.println("\tgathering arguments...");
        for (ParsedArgument arg : ctx.getArguments()) {
            Argument a = arg.getArgument();
            if (a instanceof ParameterArgument) {
                System.out.println("\t\t[" + a.name + "] = " + arg.getValue());
                args[((ParameterArgument) a).getIndex()] = arg.getValue();
            }
        }
        System.out.println("\trunning preExecute...");
        for (MethodAnnotationAdapter a : annotationAdapters) {
            try {
                System.out.println("\t\t" + a);
                List<Object> tempArgs = new ArrayList<>(Arrays.asList(args));
                a.preExecute(this, method.getAnnotation(a.getType()), tempArgs, ctx);
                args = tempArgs.toArray();
            } catch (CommandFailedException e) {
                return CommandResult.fail(e);
            }
        }
        System.out.println("\tgathering syntaxless args...");
        for (ParameterArgument a : syntaxlessArgs) {
            try {
                Object val = a.modify(a.getDefault(ctx), ctx);
                args[a.getIndex()] = val;
                System.out.println("\t\t[" + a.name + "] = " + val);
            } catch (Exception e) {
                return CommandResult.fail(e);
            }
        }
        Object ret;
        try {
            System.out.println("\tinvoking method...");
            ret = method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            ret = CommandResult.fail(e.getCause());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new CommandFailedException(e);
        }
        CommandResult<Object> result = CommandResult.from(ret);
        System.out.println("\trunning postExecute...");
        for (MethodAnnotationAdapter a : annotationAdapters) {
            System.out.println("\t\t" + a);
            a.postExecute(this,method.getAnnotation(a.getType()),result,ctx);
        }
        System.out.println("\tdone!");
        return result;
    }

    public boolean hasAnnotation(Class<? extends Annotation> a) {
        return method.isAnnotationPresent(a);
    }

    public <A extends Annotation> A getAnnotation(Class<A> a) {
        return method.getAnnotation(a);
    }


    public void addSyntaxlessArg(ParameterArgument arg) {
        this.syntaxlessArgs.add(arg);
    }

    @Override
    public String toString() {
        return "MethodCommand{" +
                "name='" + name + '\'' +
                ", method=" + method +
                ", executable=" + (getExecutor() != null) +
                "}";
    }
}
