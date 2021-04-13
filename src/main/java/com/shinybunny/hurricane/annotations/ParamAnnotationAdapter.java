package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.Hurricane;
import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.tree.ParameterArgument;

import java.lang.annotation.Annotation;

/**
 * An {@link AnnotationAdapter} for annotations declared on {@link ParameterArgument parameters in commands}.
 * This adapter can modify the value of the parsed argument, or modify the argument's settings.
 * <br/>
 * Custom ParamAnnotationAdapters can be registered to an API with {@link Hurricane#addParamAnnotationAdapter(ParamAnnotationAdapter)},
 * or simply with the {@link Adapter} annotation.
 * @param <A> The type of annotation
 */
public interface ParamAnnotationAdapter<A extends Annotation> extends AnnotationAdapter<A,ParameterArgument> {

    default Object getDefault(A annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
        return null;
    }

    void validate(Object value, A annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception;

}
