package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.Priority;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;

import java.lang.annotation.Annotation;

/**
 * The base adapter interface for custom annotations.
 * Annotations in the CommandAPI can modify the behavior and the settings of their target object.
 *
 * @param <A> The type of annotation
 * @param <T> The type of the container the annotation should be declared on
 */
public interface AnnotationAdapter<A extends Annotation,T> {

    /**
     * Gets the type of annotation this adapter should be applied to
     * @return The class of the annotation type
     */
    Class<A> getType();

    /**
     * Initializes a container with this annotation when it's being registered.
     * This method can be used to modify the container's settings according to the settings defined in the annotation.
     * @param instance The annotation instance
     * @param container The element the annotation is declared on.
     * @param ctx The current command registering context
     * @throws CommandRegisterFailedException When this annotation is found to be inapplicable to the container, or a field in the annotation is invalid.
     * Will prevent this annotation from being added to the command
     */
    void init(A instance, T container, CommandRegisteringContext ctx) throws CommandRegisterFailedException;

    /**
     * Whether this adapter can be applied to
     * @param instance The instance of the annotation
     * @param container The container the annotation is declared on
     * @param ctx The current command registering context
     * @return True if the annotation can be applied to the provided container
     */
    default boolean canApply(A instance, T container, CommandRegisteringContext ctx) {
        return true;
    }

    /**
     * Gets the {@link Priority} this adapter has on another adapter when both are declared on the same element.
     * @param other The other {@link AnnotationAdapter} in question
     * @param ctx The current command registering context
     * @return The priority this adapter has on the other. If the priority is not {@link Priority#DEFAULT}, it may change the order of the adapters being used.
     */
    default Priority getPriorityOn(AnnotationAdapter<?,?> other, CommandRegisteringContext ctx) {
        return Priority.DEFAULT;
    }

}
