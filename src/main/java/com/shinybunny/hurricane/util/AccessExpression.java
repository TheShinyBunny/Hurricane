package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.InputReader;
import com.shinybunny.hurricane.tree.ParsedArgument;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An access expression is a string expressing a value accessed from the parameters of a command, using java-like code.
 * For example, let's say we have a parameter named 'foo'. The following expressions can be used just like you would access it in java:
 * <pre>
 *     foo.getBar()
 *     foo.myField
 *     foo.myField.getBaz()
 *     foo.myArr[0].getName()
 *     foo.myList.get(0)
 *     foo.myMap.get("key")
 *     foo.getSomething(otherParam,"abc")
 * </pre>
 * All of these example would work (as long as these methods and fields exist on the class of <code>foo</code>).
 * <p>
 *     Here is a full list of things you can do:
 *     <ul>
 *         <li>access a field of an object</li>
 *         <li>invoke a method</li>
 *         <li>access an array at an index</li>
 *         <li>use primitive values: int, string, boolean, double, float, char and null as parameters in methods and array access.</li>
 *         <li>use values of other parameters, as a nested access expression.</li>
 *         <li>use <code>new my.package.SomeClass()</code> (you must include the full package!). The constructor may include any primitive or nested expression as well.</li>
 *     </ul>
 *
 *     You cannot declare any temporary variables or have any operator expressions (because that'd be too complicated to parse).
 * </p>
 */
public class AccessExpression {

    private final CommandExecutionContext ctx;
    private final InputReader reader;

    public AccessExpression(String expression, CommandExecutionContext ctx) {
        this.reader = new InputReader(expression);
        this.ctx = ctx;
    }

    public Object parseValue() throws Exception {
        reader.skipSpace();
        Object value;
        if (reader.readPhrase("new ",false)) {
            value = parseNewInstance(reader,ctx);
        } else if (reader.peek() == '"') {
            value = parseString(reader);
        } else if (reader.peek() == '\'') {
            reader.next();
            char c = reader.next();
            reader.expect('\'',"Expected ' to close char");
            return c;
        } else if ((reader.peek() == '-' && Character.isDigit(reader.peek(1))) || Character.isDigit(reader.peek())) {
            return reader.readAnyNumber();
        } else {
            Optional<String> b = reader.readOneOf(false,"true","false");
            if (b.isPresent()) {
                return Boolean.parseBoolean(b.get());
            } else if (reader.readPhrase("null",false)) {
                return null;
            } else {
                String id = reader.readIdentifier();
                if (id.isEmpty()) {
                    throw new Exception("Expected identifier at: " + reader.markerHere());
                }
                Optional<ParsedArgument> arg = ctx.getArg(id);
                if (!arg.isPresent()) {
                    throw new Exception("Unknown parameter '" + id + "'!");
                }
                value = arg.get().getValue();
            }
        }
        return parseAccess(value);
    }

    private Object parseAccess(Object value) throws Exception {
        reader.skipSpace();
        if (reader.peek() == '.') {
            if (value == null) throw new Exception("Trying to access a null value! " + reader.markerHere());
            reader.next();
            reader.skipSpace();
            String field = reader.readIdentifier();
            if (field.isEmpty()) {
                throw new Exception("Expected identifier at: " + reader.markerHere());
            }
            reader.skipSpace();
            Class<?> cls = value.getClass();
            if (reader.peek() == '(') {
                List<Method> methods = new ArrayList<>();
                methods.addAll(Arrays.asList(cls.getMethods()));
                methods.addAll(Arrays.asList(cls.getDeclaredMethods()));
                List<Method> matching = new ArrayList<>();
                for (Method m : methods) {
                    if (m.getName().equals(field)) {
                        matching.add(m);
                    }
                }
                if (matching.isEmpty()) {
                    throw new Exception("Unknown method " + field);
                }
                reader.next();
                reader.skipSpace();
                List<Object> args = readMethodParams();
                for (Method m : matching) {
                    m.setAccessible(true);
                    try {
                        value = m.invoke(value,args.toArray(new Object[0]));
                        break;
                    } catch (IllegalAccessException | IllegalArgumentException ignored) {

                    }
                }
            } else {
                List<Field> fields = new ArrayList<>();
                fields.addAll(Arrays.asList(cls.getFields()));
                fields.addAll(Arrays.asList(cls.getDeclaredFields()));
                Field matching = null;
                for (Field f : fields) {
                    if (f.getName().equals(field)) {
                        matching = f;
                    }
                }
                if (matching == null) {
                    throw new Exception("Unknown field " + field);
                }
                value = matching.get(value);
            }
        } else if (reader.peek() == '[') {
            if (value == null) throw new Exception("Trying to access a null value! " + reader.markerHere());
            if (!value.getClass().isArray()) throw new Exception("Trying to access index of a non-array value! " + reader.markerHere());
            reader.skipSpace();
            int start = reader.getPos();
            Object index = parseValue();
            if (!(index instanceof Integer)) {
                throw new Exception("Index value must be an integer! at " + reader.markerSince(start));
            }
            reader.expect(']',"Expected ]");
            value = Array.get(value,(int)index);
        } else {
            return value;
        }
        return parseAccess(value);
    }

    private String parseString(InputReader reader) {
        return reader.readQuotableString();
    }

    private Object parseNewInstance(InputReader reader, CommandExecutionContext ctx) throws Exception {
        reader.skipSpace();
        String className = reader.readUntil('(',(char) 0).trim();
        Class<?> cls = Class.forName(className);
        reader.expect('(',"Expected (");

        List<Object> args = readMethodParams();
        for (Constructor ctor : cls.getConstructors()) {
            try {
                return ctor.newInstance(args.toArray());
            } catch (Exception ignored) { }
        }
        throw new Exception("No constructor matched the parameters passed to the new " + cls.getSimpleName() + "() expression");
    }

    private List<Object> readMethodParams() throws Exception {
        List<Object> args = new ArrayList<>();
        while (reader.canRead()) {
            reader.skipSpace();
            args.add(parseValue());
            reader.skipSpace();
            if (reader.peek() != ',') {
                reader.expect(')',"Expected )");
                break;
            }
            reader.next();
        }
        return args;
    }

}
