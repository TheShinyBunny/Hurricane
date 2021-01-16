package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.arguments.ArgumentAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as a "dummy" annotation.
 * Using this in a custom annotation declaration will make the registration process assign the target annotation an empty {@link AnnotationAdapter}.
 * This can be used on annotations that do not need their own adapter, and are being used by {@link ArgumentAdapter}s or other annotations.
 *
 * @see Greedy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface DummyAdapter {

    /**
     * An optional flag ID to add to an argument annotated with an annotation declared as a DummyAdapter
     * @return A custom ID to use as a flag
     */
    String flag() default "";

}
