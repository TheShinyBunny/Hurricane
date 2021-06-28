package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.tree.MethodCommand;
import com.shinybunny.hurricane.util.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Method commands annotated with Feedback will send a response message when the command was successful or when it failed,
 * using the provided <code>success</code> and <code>failed</code> fields.
 * <p>
 *     Both strings can use {@link com.shinybunny.hurricane.util.AccessExpression}s inside with <code>${expression}</code>.
 *     <br/>
 *     If the command returned a value other than a boolean or a string, the returned value can be used in the success message with <code>${result}</code>.
 *     An access expression can be used there as well.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Adapter(Feedback.Adapter.class)
public @interface Feedback {

    /**
     * A success message to send to the executor when the command completed successfully.
     * A successful command is a command that did not end by an exception and did not return <code>false</code>.
     */
    String success() default "";

    /**
     * A failure message to send to the executor when the command failed.
     * A command fails when it threw an exception or returned <code>false</code>.
     */
    String fail() default "";

    class Adapter implements MethodAnnotationAdapter<Feedback> {

        @Override
        public void preExecute(MethodCommand cmd, Feedback annotation, List<Object> args, CommandExecutionContext ctx) {

        }

        @Override
        public void postExecute(MethodCommand cmd, Feedback annotation, CommandResult<?> result, CommandExecutionContext ctx) {
            if (!result.getMessage().isEmpty()) return;
            String msg;
            if (result.isSuccessful()) {
                msg = annotation.success();
            } else {
                msg = annotation.fail();
            }
            if (msg.isEmpty()) return;
            msg = formatMessage(msg,ctx,result.getResult());
            ctx.getSender().sendFeedback(result.isSuccessful(),msg);
        }

        private static final Pattern argPattern = Pattern.compile("(?<!\\\\)\\$\\{([^}]+)}");

        private String formatMessage(String msg, CommandExecutionContext ctx, Object result) {
            StringBuilder b = new StringBuilder(msg);
            while (true) {
                Matcher m = argPattern.matcher(b);
                if (m.find()) {
                    String arg = m.group(1);
                    try {
                        Object value;
                        if (arg.equalsIgnoreCase("result")) {
                            value = result;
                        } else {
                            value = Utils.getArgumentValueMember(arg, ctx);
                        }
                        b.delete(m.start(),m.end());
                        b.insert(m.start(), value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return b.toString();
                    }
                    continue;
                }
                return b.toString();
            }
        }

        @Override
        public Class<Feedback> getType() {
            return Feedback.class;
        }

        @Override
        public void init(Feedback instance, MethodCommand container, CommandRegisteringContext ctx) {

        }
    }

}
