package com.shinybunny.hurricane;

import com.shinybunny.hurricane.annotations.*;
import com.shinybunny.hurricane.arguments.*;
import com.shinybunny.hurricane.tree.*;
import com.shinybunny.hurricane.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The main CommandAPI instance. This is a common class storing all commands and all type adapters, serving as a command system.
 * You can create multiple command APIs to have multiple different command systems.
 * <br/>
 * <br/>
 * To register commands, call {@link #register(Object)} with a holder class for multiple method commands,
 * or call {@link #register(CommandNode)} with a built command instance.
 */
public class Hurricane {

    private List<ParamAnnotationAdapter<?>> paramAnnotationAdapters = new ArrayList<>();
    private List<MethodAnnotationAdapter<?>> methodAnnotationAdapters = new ArrayList<>();
    private List<ArgumentAdapter<?>> argumentAdapters = new ArrayList<>();
    private List<CommandNode> commands = new ArrayList<>();
    private CommandNode root = new CommandNode("");

    private boolean literalsIgnoreCase;

    private Predicate<CommandNode> commandConsumer;

    public Hurricane() {
        this(true);
    }

    public Hurricane(boolean literalsIgnoreCase) {
        this.literalsIgnoreCase = literalsIgnoreCase;
        initDefaults();
    }

    public boolean doLiteralsIgnoreCase() {
        return literalsIgnoreCase;
    }

    public void onCommandRegistered(Predicate<CommandNode> consumer) {
        commandConsumer = consumer;
    }

    public static void main(String[] args) {
        Hurricane api = new Hurricane();
        api.register(ExampleCommands.class);

        String input = "";
        Scanner in = new Scanner(System.in);
        do {
            input = in.nextLine();
            try {
                CommandResult result = api.execute(new User("[SENDER]"), input);
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!input.equalsIgnoreCase("exit"));
    }

    protected MethodCommand createFromMethod(CommandRegisteringContext ctx, Method m, CommandContainer instance) {
        MethodCommand cmd = new MethodCommand(ctx,m,instance);
        CommandNode node = cmd;
        for (int i = 0; i < m.getParameterCount(); i++) {
            Parameter p = m.getParameters()[i];
            ParameterArgument arg = new ParameterArgument(p,i);
            arg.postInit(ctx);
            if (arg.isSyntax()) {
                if (!arg.isRequired()) {
                    node.setExecutor(cmd);
                }
                node.addChild(arg);
                node = arg;
            } else {
                cmd.addSyntaxlessArg(arg);
            }
        }
        cmd.postInit(ctx);
        return cmd;
    }

    protected void initDefaults() {
        addArgumentAdapter(new StringAdapter());
        addArgumentAdapter(new BooleanAdapter());
        addArgumentAdapter(Integer.class, InputReader::readInteger);
        addArgumentAdapter(Double.class, InputReader::readDouble);
        addArgumentAdapter(Long.class, InputReader::readLong);
        addArgumentAdapter(new User.Adapter());
        addArgumentAdapter(new EnumAdapter());
        addArgumentAdapter(new CommandSenderAdapter());

        addParamAnnotationAdapter(new NullableAdapter());
    }


    public void register(Object holder) {
        Class<?> cls = holder instanceof Class ? (Class<?>)holder : holder.getClass();
        if (cls.isInterface() || cls.isArray() || cls.isAnnotation()) {
            throw new RuntimeException(new CommandRegisterFailedException("Invalid command container, can only register classes or enum classes."));
        }
        CommandContainer container = new CommandContainer(this,holder);
        if (cls.isAnnotationPresent(Command.class)) {
            register(createTree(container,cls.getAnnotation(Command.class)));
        } else {
            createFromContainer(container,this::register);
        }
    }

    public void createFromContainer(CommandContainer container, Consumer<CommandNode> consumer) {
        Class<?> cls = container.getContainingClass();
        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                CommandRegisteringContext ctx = new CommandRegisteringContext(this,container,Utils.getName(m));
                CommandNode cmd = createFromMethod(ctx,m,container);
                if (!ctx.isCancelled()) {
                    consumer.accept(cmd);
                }
                ctx.printErrors();
            }
        }
        for (Class<?> sc : cls.getDeclaredClasses()) {
            if (sc.isAnnotationPresent(Command.class)) {
                consumer.accept(createTree(new CommandContainer(this,sc),sc.getAnnotation(Command.class)));
            }
        }
    }

    public void register(CommandNode cmd) {
        if (commandConsumer == null || commandConsumer.test(cmd)) {
            System.out.println("added command");
            System.out.println(cmd);
            commands.add(cmd);
            root.addChild(cmd);
        }
    }

    public CommandNode createTree(CommandContainer container, Command settings) {
        Class<?> cls = container.getContainingClass();
        CommandNode cmd = new CommandNode(container.getName(settings));
        for (Method m : cls.getDeclaredMethods()) {
            CommandRegisteringContext ctx = new CommandRegisteringContext(this,container,Utils.getName(m));
            CommandNode node = createFromMethod(ctx,m,container);
            if (!ctx.isCancelled()) {
                if (m.isAnnotationPresent(DefaultSubCommand.class)) {
                    for (CommandNode gc : node.getChildren()) {
                        cmd.addChild(gc);
                    }
                }
                cmd.addChild(node);
            }
            ctx.printErrors();
        }
        return cmd;
    }

    public ArgumentAdapter<?> getArgumentAdapter(Argument arg, CommandRegisteringContext ctx) {
        List<ArgumentAdapter<?>> adapters = argumentAdapters.stream().filter(a->a.canApply(arg,ctx)).collect(Collectors.toList());
        Random r = new Random();
        for (int i = 0; i < adapters.size() - 1; i++) {
            ArgumentAdapter<?> a = adapters.get(i);
            for (int j = i + 1; j < adapters.size(); j++) {
                ArgumentAdapter<?> b = adapters.get(j);
                switch (a.getPriorityOn(b,ctx)) {
                    case DEFAULT:
                    case BEFORE:
                        break;
                    case AFTER:
                        adapters.set(j,a);
                        adapters.set(i,b);
                        break;
                    case RANDOM:
                        if (r.nextBoolean()) {
                            adapters.set(j,a);
                            adapters.set(i,b);
                        }
                        break;
                    case OVERSHADOW:
                        adapters.remove(j);
                        j++;
                        break;
                }
                switch (b.getPriorityOn(a,ctx)) {
                    case DEFAULT:
                    case AFTER:
                        break;
                    case BEFORE:
                        adapters.set(j,a);
                        adapters.set(i,b);
                        break;
                    case RANDOM:
                        if (r.nextBoolean()) {
                            adapters.set(j,a);
                            adapters.set(i,b);
                        }
                        break;
                    case OVERSHADOW:
                        adapters.remove(i);
                        i--;
                        break;
                }
            }
        }
        return adapters.isEmpty() ? null : adapters.get(0);
    }

    public ParamAnnotationAdapter<?> getParamAnnotationAdapter(Class<? extends Annotation> annotationType) {
        for (ParamAnnotationAdapter<?> a : paramAnnotationAdapters) {
            if (a.getType() == annotationType) return a;
        }
        return null;
    }

    public MethodAnnotationAdapter<?> getMethodAnnotationAdapter(Class<? extends Annotation> annotationType) {
        for (MethodAnnotationAdapter<?> a : methodAnnotationAdapters) {
            if (a.getType() == annotationType) return a;
        }
        return null;
    }

    public void addArgumentAdapter(ArgumentAdapter<?> adapter) {
        argumentAdapters.add(adapter);
    }

    public <T> void addArgumentAdapter(Class<T> type, ArgumentParser<T> parser) {
        addArgumentAdapter(new SimpleArgumentAdapter<>(type,parser));
    }

    public void addParamAnnotationAdapter(ParamAnnotationAdapter<?> adapter) {
        paramAnnotationAdapters.add(adapter);
    }

    public void addMethodAnnotationAdapter(MethodAnnotationAdapter<?> adapter) {
        methodAnnotationAdapters.add(adapter);
    }

    public void removeArgumentAdapter(ArgumentAdapter<?> adapter) {
        argumentAdapters.remove(adapter);
    }

    public void removeParamAnnotationAdapter(ParamAnnotationAdapter<?> adapter) {
        paramAnnotationAdapters.remove(adapter);
    }

    public void removeMethodAnnotationAdapter(MethodAnnotationAdapter<?> adapter) {
        methodAnnotationAdapters.remove(adapter);
    }

    public void removeArgumentAdapters(Class<?> type) {
        argumentAdapters.removeIf(a->type.isAssignableFrom(a.getType()));
    }

    public void removeParamAnnotationAdapters(Class<? extends Annotation> annotationType) {
        paramAnnotationAdapters.removeIf(a->a.getType() == annotationType);
    }

    public void removeMethodAnnotationAdapters(Class<? extends Annotation> annotationType) {
        methodAnnotationAdapters.removeIf(a->a.getType() == annotationType);
    }

    public ParseResult parse(CommandSender sender, String input) {
        InputReader reader = new InputReader(input);
        CommandExecutionContext ctx = new CommandExecutionContext(this,sender,reader,null);
        return parseNodes(root, reader, ctx);
    }

    private ParseResult parseNodes(CommandNode node, InputReader originalReader, CommandExecutionContext builder) {
        CommandSender sender = builder.getSender();
        Map<CommandNode, CommandParsingException> errors = null;
        List<ParseResult> potentials = null;
        int pos = originalReader.getPos();
        for (CommandNode child : node.getRelevantNodes(this,originalReader)) {
            if (!child.canUse(sender)) continue;
            CommandExecutionContext ctx = builder.copy();
            InputReader reader = originalReader.copy();
            try {
                child.parse(reader,ctx);
                if (reader.canRead()) {
                    if (reader.peek() != ' ') {
                        throw new CommandParsingException("Expected argument separator",reader.markerHere());
                    }
                }
            } catch (CommandParsingException e) {
                if (errors == null) {
                    errors = new HashMap<>();
                }
                errors.put(child,e);
                reader.setPos(pos);
                continue;
            }
            if (child.getExecutor() != null) {
                ctx.withExecutor(child.getExecutor());
            }
            reader.next();
            ParseResult res = parseNodes(child, reader, ctx);
            if (potentials == null) {
                potentials = new ArrayList<>(1);
            }
            potentials.add(res);
        }

        if (potentials != null) {
            if (potentials.size() > 1) {
                potentials.sort((a,b)->{
                    if (!a.getReader().canRead() && b.getReader().canRead()) return -1;
                    if (a.getReader().canRead() && !b.getReader().canRead()) return 1;
                    if (a.getExceptions().isEmpty() && !b.getExceptions().isEmpty()) return -1;
                    if (!a.getExceptions().isEmpty() && b.getExceptions().isEmpty()) return 1;
                    return 0;
                });
            }
            return potentials.get(0);
        }
        return new ParseResult(builder,originalReader,errors == null ? new HashMap<>() : errors);
    }

    public CommandResult<?> execute(CommandSender sender, String input) throws CommandParsingException {
        ParseResult res = parse(sender,input);
        return execute(res);
    }

    public CommandResult<?> execute(ParseResult res) throws CommandParsingException {
        if (res.getReader().canRead()) {
            if (res.getExceptions().size() == 1) {
                throw res.getExceptions().values().iterator().next();
            } else {
                throw new CommandParsingException("Unknown argument",res.getReader().markerHere());
            }
        }
        if (res.getContext().getExecutor() != null) {
            return res.getContext().getExecutor().execute(res.getContext());
        }
        throw new CommandParsingException("Invalid command",res.getReader().markerSince(0));
    }

    public List<String> getSuggestions(ParseResult parse, int cursor) {
        return new ArrayList<>(); // TODO: 15/01/2021 implement
    }

    public CommandNode getCommand(String name) {
        for (CommandNode cmd : commands) {
            if (cmd.getName().equals(name)) {
                return cmd;
            }
        }
        return null;
    }

    public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>() {
        {
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(short.class, Short.class);
            put(void.class, Void.class);
        }
    };

    public static Class<?> getPrimitiveWrapper(Class<?> type) {
        if (!type.isPrimitive()) return type;
        return PRIMITIVES_TO_WRAPPERS.get(type);
    }

    public boolean literalsEqual(String a, String b) {
        return literalsIgnoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
    }
}
