package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Adapter(Options.Adapter.class)
public @interface Options {

    String[] value() default {};

    int[] intValues() default {};

    long[] longValues() default {};

    short[] shortValues() default {};

    byte[] byteValues() default {};

    double[] doubleValues() default {};

    float[] floatValues() default {};

    char[] charValues() default {};

    class Adapter implements ParamAnnotationAdapter<Options> {

        @Override
        public Class<Options> getType() {
            return Options.class;
        }

        @Override
        public void init(Options instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {

        }

        private <T> void checkValidity(T value, Object options, BiFunction<Object,Integer,T> getter) throws Exception {
            List<String> optStr = new ArrayList<>();
            int len = Array.getLength(options);
            for (int i = 0; i < len; i++) {
                T opt = getter.apply(options,i);
                optStr.add(opt.toString());
                if (opt.equals(value)) return;
            }
            throw new Exception(String.join(", ",optStr));
        }

        @Override
        public void validate(Object value, Options annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            try {
                if (annotation.value().length > 0) checkValidity(value.toString(),annotation.value(),(o,i)->Array.get(o,i).toString());
                else if (argument.getType() == Integer.class) checkValidity(value,annotation.intValues(),Array::getInt);
                else if (argument.getType() == Double.class) checkValidity(value,annotation.doubleValues(),Array::getDouble);
                else if (argument.getType() == Float.class) checkValidity(value,annotation.floatValues(),Array::getFloat);
                else if (argument.getType() == Short.class) checkValidity(value,annotation.shortValues(),Array::getShort);
                else if (argument.getType() == Long.class) checkValidity(value,annotation.longValues(),Array::getLong);
                else if (argument.getType() == Byte.class) checkValidity(value,annotation.byteValues(),Array::getByte);
                else if (argument.getType() == Character.class) checkValidity(value,annotation.charValues(),Array::getChar);
            } catch (Exception e) {
                throw new Exception("Argument " + argument.getName() + " should be one of: " + e.getMessage());
            }
        }
    }

}
