package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

/**
 * Defines a default value for an argument parameter. Using this annotation will make the argument optional,
 * and provide the value specified in this annotation instead.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Adapter(Default.Adapter.class)
public @interface Default {

    String value() default "";

    int integer() default 0;

    double doubleValue() default 0;

    float floatValue() default 0;

    short shortValue() default 0;

    byte byteValue() default 0;

    long longValue() default 0;

    boolean bool() default false;



    /**
     * A string representing the call path to a field of another argument to use as the value.
     * <p>
     * For example, here is a command implementation of the {@link String#substring(int) String.substring()} method
     * <pre>
     * &#64;Command
     * public void substring(String str, int start, @Default(computed = "str.length()") int end) {
     *     str.substring(start,end);
     * }
     * </pre>
     * Note that in order for the expression to recognize the name of the argument (in the case of the example, 'str'),
     * parameter names need to be included in the compiled Java code, using the <code>-parameters</code> JVM argument.
     * Otherwise, the name of the parameter argument will be arg0, arg1, arg2... according to its position in the signature.
     */
    String computed() default "";

    class Adapter implements ParamAnnotationAdapter<Default> {

        @Override
        public Class<Default> getType() {
            return Default.class;
        }

        @Override
        public void init(Default instance, ParameterArgument container, CommandRegisteringContext ctx) {
            container.setRequired(false);
        }

        @Override
        public Object getDefault(Default annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            if (!annotation.computed().isEmpty()) {
                return computeValue(annotation.computed(),ctx);
            }
            if (argument.getType() == Integer.class) return annotation.value().isEmpty() ? annotation.integer() : Integer.parseInt(annotation.value());
            if (argument.getType() == Double.class) return annotation.value().isEmpty() ? annotation.doubleValue() : Double.parseDouble(annotation.value());
            if (argument.getType() == Boolean.class) return annotation.value().isEmpty() ? annotation.bool() : Boolean.parseBoolean(annotation.value());
            if (argument.getType() == Short.class) return annotation.value().isEmpty() ? annotation.shortValue() : Short.parseShort(annotation.value());
            if (argument.getType() == Byte.class) return annotation.value().isEmpty() ? annotation.byteValue() : Byte.parseByte(annotation.value());
            if (argument.getType() == Long.class) return annotation.value().isEmpty() ? annotation.longValue() : Long.parseLong(annotation.value());
            if (argument.getType() == Float.class) return annotation.value().isEmpty() ? annotation.floatValue() : Float.parseFloat(annotation.value());

            if (argument.getType() == String.class) return annotation.value();
            if (argument.getType() == Character.class) return annotation.value().charAt(0);
            if (argument.getType().isEnum()) return Enum.valueOf((Class)argument.getType(),annotation.value());
            return null;
        }

        @Override
        public void validate(Object value, Default annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {

        }


        private Object computeValue(String expr, CommandExecutionContext ctx) throws Exception {
            try {
                return Utils.getArgumentValueMember(expr,ctx);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                throw new Exception("An error occurred while computing @Default value",e);
            }
        }
    }

}
