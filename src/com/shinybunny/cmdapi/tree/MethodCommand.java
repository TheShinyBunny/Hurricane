package com.shinybunny.cmdapi.tree;

import com.shinybunny.cmdapi.*;
import com.shinybunny.cmdapi.annotations.AnnotationAdapterContainer;
import com.shinybunny.cmdapi.annotations.Command;
import com.shinybunny.cmdapi.annotations.MethodAnnotationAdapter;
import com.shinybunny.cmdapi.util.*;
import com.sun.deploy.net.MessageHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class representing a registered command that was interpreted from a method declaration.
 * <br/>
 * Method commands are created when registering a holder class through {@link CommandAPI#register(Object)}.
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
        Object instance = Modifier.isStatic(method.getModifiers()) ? null : container.getInstance(ctx);
        Object[] args = new Object[method.getParameterCount()];
        for (ParsedArgument arg : ctx.getArguments()) {
            Argument a = arg.getArgument();
            if (a instanceof ParameterArgument) {
                args[((ParameterArgument) a).getIndex()] = arg.getValue();
            }
        }
        for (MethodAnnotationAdapter a : annotationAdapters) {
            try {
                List<Object> tempArgs = new ArrayList<>(Arrays.asList(args));
                a.preExecute(this, method.getAnnotation(a.getType()), tempArgs, ctx);
                args = tempArgs.toArray();
            } catch (CommandFailedException e) {
                return CommandResult.fail(e);
            }
        }
        for (ParameterArgument a : syntaxlessArgs) {
            try {
                args[a.getIndex()] = a.modify(a.getDefault(ctx),ctx);
            } catch (Exception e) {
                return CommandResult.fail(e);
            }
        }
        Object ret;
        try {
            ret = method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            ret = CommandResult.fail(e.getCause());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CommandFailedException(e);
        }
        CommandResult<Object> result = CommandResult.from(ret);
        for (MethodAnnotationAdapter a : annotationAdapters) {
            a.postExecute(this,method.getAnnotation(a.getType()),result,ctx);
        }
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
}
