package com.shinybunny.hurricane.util;

import java.util.Collection;
import java.util.stream.Collectors;

public class IndentedPrinter {

    private final int size;
    private String indent;
    private String result;

    public IndentedPrinter(int size) {
        indent = "";
        result = "";
        this.size = size;
    }

    public void write(String title, Object... data) {
        enter(title);
        for (int i = 0; i < data.length; i++) {
            Object s = data[i];
            if (s instanceof String) {
                if (i < data.length - 1) {
                    field((String)s,data[++i]);
                } else {
                    field((String)s,null);
                }
            } else if (s != null) {
                field("",s);
            }
        }
        leave();
    }

    public void field(String name, Object value) {
        result += '\n';
        if (value == null) {
            result += indent + name;
        } else {
            result += indent;
            if (!name.isEmpty()) {
                result += name + " = ";
            }
            if (value instanceof Instance) {
                ((Instance) value).print(this);
            } else if (value instanceof Collection) {
                result += "[";
                if (!((Collection<?>) value).isEmpty()) {
                    if (((Collection<?>) value).iterator().next().getClass().isPrimitive()) {
                        result += ((Collection<?>) value).stream().map(String::valueOf).collect(Collectors.joining(", "));
                    } else {
                        increaseIndent();
                        for (Object o : (Collection) value) {
                            field("", o);
                        }
                        result += '\n';
                        decreaseIndent();
                        result += indent;
                    }
                }
                result += ']';
            } else {
                result += value;
            }
        }
    }

    private void increaseIndent() {
        for (int i = 0; i < size; i++) {
            indent += " ";
        }
    }

    private void decreaseIndent() {
        if (indent.length() > 0) {
            indent = indent.substring(0,indent.length() - size);
        }
    }

    public void enter(String title) {
        result += title + " {";
        increaseIndent();
    }

    public void leave() {
        result += '\n';
        decreaseIndent();
        result += indent + "}";
    }

    public interface Instance {
        void print(IndentedPrinter printer);
    }

    public String getResult() {
        return result;
    }
}
