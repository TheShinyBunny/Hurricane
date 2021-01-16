package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.tree.MethodCommand;
import com.shinybunny.hurricane.util.CommandFailedException;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * An {@link AnnotationAdapter} for annotations declared on {@link MethodCommand method commands}.
 * This adapter can modify the behavior of the executed command or its settings when registered.
 * <br/>
 * Custom MethodAnnotationAdapters can be registered to an API with {@link CommandAPI#addMethodAnnotationAdapter(MethodAnnotationAdapter)}
 * @param <A> The type of the annotation
 */
public interface MethodAnnotationAdapter<A extends Annotation> extends AnnotationAdapter<A,MethodCommand> {

    void preExecute(MethodCommand cmd, A annotation, List<Object> args, CommandExecutionContext ctx) throws CommandFailedException;

    void postExecute(MethodCommand cmd, A annotation, CommandResult<?> result, CommandExecutionContext ctx) throws CommandFailedException;

    default void postInit(A annotation, MethodCommand cmd, CommandRegisteringContext ctx) throws CommandRegisterFailedException {

    }
}
