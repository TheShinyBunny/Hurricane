package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandAPI;
import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.tree.ParameterArgument;

import java.lang.annotation.Annotation;

/**
 * An {@link AnnotationAdapter} for annotations declared on {@link ParameterArgument parameters in commands}.
 * This adapter can modify the value of the parsed argument, or modify the argument's settings.
 * <br/>
 * Custom ParamAnnotationAdapters can be registered to an API with {@link CommandAPI#addParamAnnotationAdapter(ParamAnnotationAdapter)},
 * or simply with the {@link Adapter} annotation.
 * @param <A> The type of annotation
 */
public interface ParamAnnotationAdapter<A extends Annotation> extends AnnotationAdapter<A,ParameterArgument> {

    /**
     * Validates the value of an argument and possibly modifying it to a different value.
     * @param value The value so far that was parsed from the input or returned from previous annotation adapters.
     * @return <code>null</code> or the same value to make no change. Any other value will be used for the command instead.
     */
    Object modify(Object value, A annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception;

}
