package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandExecutionContext;
import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.tree.ParameterArgument;
import com.shinybunny.cmdapi.util.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Adapter(Range.Adapter.class)
public @interface Range {

    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;

    class Adapter implements ParamAnnotationAdapter<Range> {

        @Override
        public Object modify(Object value, Range annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            if (value instanceof Number) {
                double d = ((Number)value).doubleValue();
                if (annotation.min() > d) {
                    throw new Exception(argument.getName() + " must be greater than " + Utils.formatDouble(annotation.min()));
                }
                if (annotation.max() < d) {
                    throw new Exception(argument.getName() + " must be less than " + Utils.formatDouble(annotation.max()));
                }
            }
            return null;
        }

        @Override
        public Class<Range> getType() {
            return Range.class;
        }

        @Override
        public boolean canApply(Range instance, ParameterArgument container, CommandRegisteringContext ctx) {
            return container.instanceOf(Number.class);
        }

        @Override
        public void init(Range instance, ParameterArgument container, CommandRegisteringContext ctx) {

        }
    }

}
