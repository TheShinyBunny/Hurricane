package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Adapter(Condition.Adapter.class)
public @interface Condition {

    String value();

    Comparison op();

    class Adapter implements ParamAnnotationAdapter<Condition> {

        @Override
        public Class<Condition> getType() {
            return Condition.class;
        }

        @Override
        public void init(Condition instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {

        }

        @Override
        public void validate(Object value, Condition annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {

        }
    }

    enum Comparison {
        LESS_THAN(Number.class,(a,b)->a.doubleValue() < b.doubleValue(),"%s must be less than %d"),
        GREATER_THAN(Number.class,(a,b)->a.doubleValue() > b.doubleValue(),"%s must be greater than %d"),
        LESS_EQUAL(Number.class,(a,b)->a.doubleValue() <= b.doubleValue(),"%s must be less than or equal to %d"),
        GREATER_EQUAL(Number.class,(a,b)->a.doubleValue() >= b.doubleValue(),"%s must be greater than or equal to %d"),
        NOT_EQUAL(Object.class,(a,b)->!Objects.equals(a,b),"%s must not equal %s"),
        CONTAINS(String.class, String::contains,"%s must contain %s"),
        CONTAINS_REGEX(String.class, (a,b)->{
            Matcher m = Pattern.compile(b).matcher(a);
            return m.find();
        },"%s does not contain the regex %s"),
        MATCHES_REGEX(String.class, String::matches,"%s must match regex %s");

        private Class<?> valueSuperType;
        private BiPredicate<?,?> predicate;
        private String errorMessage;

        <T> Comparison(Class<T> valueSuperType, BiPredicate<T, T> predicate, String errorMessage) {
            this.valueSuperType = valueSuperType;
            this.predicate = predicate;
            this.errorMessage = errorMessage;
        }

        public void validate(Object target, Object value) throws Exception {

        }

    }
}
