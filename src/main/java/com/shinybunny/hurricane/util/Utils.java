package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.annotations.Command;
import com.shinybunny.hurricane.tree.ParsedArgument;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Utils {


    public static String formatDouble(double d) {
        if (d % 1.0 != 0) return String.format("%s",d);
        return String.format("%.0f",d);
    }

    public static String getName(Method m) {
        if (m.isAnnotationPresent(Command.class)) {
            String name = m.getAnnotation(Command.class).value();
            if (!name.isEmpty()) return name;
        }
        return m.getName();
    }

    public static Object getArgumentValueMember(String expr, CommandExecutionContext ctx) throws Exception {
        Object value = null;
        boolean first = true;
        String str = expr;
        while (!str.isEmpty() && (first || value != null)) {
            int dot = str.indexOf('.');
            String node = str.substring(0,dot < 0 ? str.length() : dot);
            if (first) {
                Optional<ParsedArgument> entry = ctx.getArg(node);
                if (entry.isPresent()) {
                    value = entry.get().getValue();
                } else {
                    throw new Exception("Unknown argument named " + node);
                }
            } else {
                if (node.endsWith("()")) {
                    Method m = value.getClass().getMethod(node.substring(0,node.length()-2));
                    m.setAccessible(true);
                    if (m.getReturnType() != Void.TYPE && m.getParameterCount() == 0) {
                        value = m.invoke(value);
                    }
                } else {
                    Field f = value.getClass().getField(node);
                    f.setAccessible(true);
                    value = f.get(value);
                }
            }
            if (dot < 0) break;
            str = str.substring(dot+1);
            first = false;
        }
        if (value == null) {
            return "unknown";
        }
        return value;
    }

    public static String[] toStringArr(Object array) {
        int n = Array.getLength(array);
        List<String> strs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Object o = Array.get(array, i);
            strs.add(o.toString());
        }
        return strs.toArray(new String[0]);
    }
}
