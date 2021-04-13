package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.Hurricane;
import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.annotations.AnnotationAdapter;
import com.shinybunny.hurricane.annotations.AnnotationAdapterContainer;
import com.shinybunny.hurricane.annotations.ParamAnnotationAdapter;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;
import com.shinybunny.hurricane.util.CustomDataHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Represents an {@link Argument} created from a method {@link Parameter}.
 * <br/>
 * Parameter arguments are different from normal arguments as they can use custom annotations.
 * Each annotation attached to the parameter uses a registered {@link ParamAnnotationAdapter} from the {@link Hurricane}.
 * These adapters can listen to the annotation being attached to a ParameterArgument, and modify the argument's behavior.
 */
public class ParameterArgument extends Argument implements AnnotationAdapterContainer<ParamAnnotationAdapter> {

    /**
     * The {@link Parameter} this argument is created from
     */
    private final Parameter parameter;
    /**
     * A sorted list of {@link ParamAnnotationAdapter}s created from the annotations declared on the parameter.
     * The list is sorted by their {@link AnnotationAdapter#getPriorityOn(AnnotationAdapter, CommandRegisteringContext) priority} value
     */
    private List<ParamAnnotationAdapter> annotationAdapters;
    /**
     * The index of the {@link #parameter} in the declaring method
     */
    private final int index;

    public ParameterArgument(Parameter parameter, int index) {
        super(parameter.getName(),parameter.getType());
        this.parameter = parameter;
        this.index = index;
    }

    @Override
    public void postInit(CommandRegisteringContext ctx) {
        super.postInit(ctx);
        if (annotationAdapters != null) return;
        annotationAdapters = resolveAnnotationAdapters(ctx);
        try {
            for (ParamAnnotationAdapter a : annotationAdapters) {
                a.init(parameter.getAnnotation(a.getType()),this,ctx);
            }
        } catch (CommandRegisterFailedException e) {
            ctx.addError(e);
        }
    }

    @Override
    public void validate(Object obj, CommandExecutionContext ctx) throws Exception {
        for (ParamAnnotationAdapter a : annotationAdapters) {
            a.validate(obj,getAnnotation(a),this,ctx);
        }
    }

    @Override
    public Object getDefault(CommandExecutionContext ctx) throws Exception {
        for (ParamAnnotationAdapter a : annotationAdapters) {
            Object def = a.getDefault(getAnnotation(a), this, ctx);
            if (def != null) return def;
        }
        return getAdapter().getDefault(ctx);
    }

    private Annotation getAnnotation(ParamAnnotationAdapter a) {
        return parameter.getAnnotation(a.getType());
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return parameter.isAnnotationPresent(annotationType);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return parameter.getAnnotation(annotationType);
    }

    @Override
    public ParamAnnotationAdapter dummyAdapter(Class<? extends Annotation> annotationType) {
        return new ParamAnnotationAdapter() {

            @Override
            public void validate(Object value, Annotation annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {

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
    public ParamAnnotationAdapter getAdapterFor(CommandRegisteringContext ctx, Class<? extends Annotation> annotationType) {
        return ctx.getApi().getParamAnnotationAdapter(annotationType);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return parameter;
    }

    @Override
    public CustomDataHolder getDataHolder() {
        return this;
    }

    /**
     * Get the index of the underlying parameter in the method
     * @return A zero-based index of the parameter position.
     */
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "ParameterArgument(" +
                "name=" + name +
                ", type=" + getType() +
                ", required=" + isRequired() +
                ", parameter=" + parameter +
                ", index=" + index +
                ", executable=" + (getExecutor() != null) +
                ")";
    }
}
