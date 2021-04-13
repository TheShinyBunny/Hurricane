package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.Utils;

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
        public void validate(Object value, Range annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            if (value instanceof Number) {
                double d = ((Number)value).doubleValue();
                if (annotation.min() > d) {
                    throw new Exception(argument.getName() + " must be greater than " + Utils.formatDouble(annotation.min()));
                }
                if (annotation.max() < d) {
                    throw new Exception(argument.getName() + " must be less than " + Utils.formatDouble(annotation.max()));
                }
            }
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
