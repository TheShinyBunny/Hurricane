package com.shinybunny.hurricane.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on annotations to automatically define their {@link AnnotationAdapter} class.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Adapter {

    Class<? extends AnnotationAdapter> value();

}
