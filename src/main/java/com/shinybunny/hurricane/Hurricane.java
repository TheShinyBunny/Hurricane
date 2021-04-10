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
 * The main Hurricane API instance. This is a common class storing all commands and all type adapters, serving as a command system.
 * You can create multiple instances of this class to have multiple different command systems.
 * <br/>
 * To register commands, call {@link #register(Object)} with a container class for multiple method commands,
 * or call {@link #register(CommandNode)} with a built command instance.
 */
public class Hurricane {

    private List<ParamAnnotationAdapter<?>> paramAnnotationAdapters = new ArrayList<>();
    private List<MethodAnnotationAdapter<?>> methodAnnotationAdapters = new ArrayList<>();
    private List<ArgumentAdapter<?>> argumentAdapters = new ArrayList<>();
    private final CommandNode root = new CommandNode("");

    private boolean literalsIgnoreCase = true;
    private boolean allowMultiSpaces = true;

    private Predicate<CommandNode> commandConsumer;
    private Consumer<String> logger;

    public Hurricane() {
        initDefaults();
    }

    /**
     * Determines if literal arguments (usually command names and sub-commands) ignore capitalization.
     * That means the input <code>HELP</code> will run the command <code>help</code>.
     * @param literalsIgnoreCase True to ignore case when matching literal strings
     */
    public void setLiteralsIgnoreCase(boolean literalsIgnoreCase) {
        this.literalsIgnoreCase = literalsIgnoreCase;
    }

    /**
     * Determines if arguments in a command can be separated by more than 1 whitespace character.
     * @param allowMultiSpaces True to allow multiple spaces between parsed arguments
     */
    public void setAllowMultiSpaces(boolean allowMultiSpaces) {
        this.allowMultiSpaces = allowMultiSpaces;
    }

    public boolean isAllowMultiSpaces() {
        return allowMultiSpaces;
    }

    public boolean areLiteralsIgnoreCase() {
        return literalsIgnoreCase;
    }

    /**
     * Sets a callback to when a new command has been registered to this API instance.
     * @param consumer The callback, with the registered {@link CommandNode} as the parameter. Returning false will cancel the command registration.
     */
    public void onCommandRegistered(Predicate<CommandNode> consumer) {
        commandConsumer = consumer;
    }

    /**
     * Sets a callback to logging messages. Good for debugging the API.
     */
    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public Consumer<String> getLogger() {
        return logger;
    }

    public void log(String msg) {
        if (logger != null) {
            logger.accept(msg);
        }
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
            if (!arg.isRequired() || !arg.isSyntax()) {
                node.setExecutor(cmd);
            }
            node.addChild(arg);
            node = arg;
        }
        node.setExecutor(cmd);
        cmd.postInit(ctx);
        return cmd;
    }

    protected void initDefaults() {
        addArgumentAdapter(new StringAdapter());
        addArgumentAdapter(new BooleanAdapter());
        addArgumentAdapter(Integer.class, InputReader::readInteger);
        addArgumentAdapter(Double.class, InputReader::readDouble);
        addArgumentAdapter(Long.class, InputReader::readLong);
        addArgumentAdapter(Short.class, InputReader::readShort);
        addArgumentAdapter(Byte.class, InputReader::readByte);
        addArgumentAdapter(Float.class, InputReader::readFloat);
        addArgumentAdapter(new User.Adapter());
        addArgumentAdapter(new EnumAdapter());
        addArgumentAdapter(new CommandSenderAdapter());
        addArgumentAdapter(new ContextAdapter());
    }

    /**
     * Registers an entire class containing multiple methods annotated with {@link Command}.
     * @param container The container class or an instance of the class
     */
    public void register(Object container) {
        Class<?> cls = container instanceof Class ? (Class<?>)container : container.getClass();
        if (cls.isInterface() || cls.isArray() || cls.isAnnotation()) {
            throw new RuntimeException(new CommandRegisterFailedException("Invalid command container, can only register classes or enum classes."));
        }
        CommandContainer c = new CommandContainer(this,container);
        if (cls.isAnnotationPresent(Command.class)) {
            register(createTree(c,cls.getAnnotation(Command.class)));
        } else {
            createFromContainer(c,this::register);
        }
    }

    protected void createFromContainer(CommandContainer container, Consumer<CommandNode> consumer) {
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

    /**
     * Registers a single command to the command tree.
     * <p>
     *     If there is a {@link #onCommandRegistered(Predicate) command registered} consumer set,
     *     Will only register the command if that predicate returned <code>true</code>.
     * </p>
     * @param cmd The root {@link CommandNode} of the command.
     */
    public void register(CommandNode cmd) {
        if (commandConsumer == null || commandConsumer.test(cmd)) {
            log("Added command: " + cmd);
            getRoot().addChild(cmd);
        }
    }

    public CommandNode createTree(CommandContainer container, Command settings) {
        Class<?> cls = container.getContainingClass();
        CommandNode cmd = new CommandNode(container.getName(settings));
        cmd.setDescription(settings.desc());
        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                CommandRegisteringContext ctx = new CommandRegisteringContext(this, container, Utils.getName(m));
                CommandNode node = createFromMethod(ctx, m, container);
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

    /**
     * Parses a command input, ran by the passed {@link CommandSender}.
     * @param sender The entity executing the command
     * @param input The command input
     * @return An object representing the compiled parsing results, to be saved for later or passed to {@link #execute(ParseResult)}.
     */
    public ParseResult parse(CommandSender sender, String input) {
        log("parsing command: " + input);
        InputReader reader = new InputReader(input);
        CommandExecutionContext ctx = new CommandExecutionContext(this,sender,reader,null);
        return parseNodes(getRoot(), reader, ctx);
    }

    private ParseResult parseNodes(CommandNode node, InputReader originalReader, CommandExecutionContext builder) {
        CommandSender sender = builder.getSender();
        Map<CommandNode, CommandParsingException> errors = null;
        List<ParseResult> potentials = null;
        int pos = originalReader.getPos();
        for (CommandNode child : node.getRelevantNodes(this,originalReader)) {
            if (!child.canUse(sender)) continue;
            log("trying to parse node " + child);
            CommandExecutionContext ctx = builder.copy();
            InputReader reader = originalReader.copy();
            try {
                child.parse(reader,ctx);
                if (reader.canRead()) {
                    if (reader.peek() != ' ' && child.isSyntax() && child.needsSpaceAfter()) {
                        throw new CommandParsingException("Expected a space to end argument",reader.markerHere());
                    }
                }
            } catch (CommandParsingException e) {
                if (errors == null) {
                    errors = new HashMap<>();
                }
                log("added error: " + e);
                errors.put(child,e);
                reader.setPos(pos);
                continue;
            }
            if (child.isSyntax()) {
                if (reader.canRead()) {
                    if (isAllowMultiSpaces()) {
                        reader.skipSpace();
                    } else if (reader.peek() == ' ') {
                        reader.next();
                    }
                }
                ctx.withExecutor(child.getExecutor());
            }
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

    /**
     * Parses and executes a command input.
     * @param sender The source of execution
     * @param input The command input line to parse
     * @return A {@link CommandResult} with info about the results of running the command.
     * @throws CommandParsingException When an error occurs while parsing the command input. Can be caught and sent to the user to inform about syntax errors.
     * @throws CommandFailedException When an <b>unexpected</b> error occurs while executing the command.
     */
    public CommandResult<?> execute(CommandSender sender, String input) throws CommandParsingException, CommandFailedException {
        ParseResult res = parse(sender,input);
        return execute(res);
    }

    /**
     * Executes a command from a {@link ParseResult} object.
     * @param res The result object returned from {@link #parse(CommandSender, String)}
     * @return A {@link CommandResult} with info about the results of running the command.
     * @throws CommandParsingException When the parse results have an error it will throw it
     * @throws CommandFailedException When an <b>unexpected</b> error occurs while executing the command.
     */
    public CommandResult<?> execute(ParseResult res) throws CommandParsingException, CommandFailedException {
        if (!res.getExceptions().isEmpty()) {
            if (res.getExceptions().size() == 1) {
                throw res.getExceptions().values().iterator().next(); // TODO: 19/01/2021 make a multi-error for multiple errors
            } else {
                throw new CommandParsingException("Unknown argument",res.getReader().markerHere());
            }
        }
        log("executing command /" + res.getReader().getString());
        if (res.getContext().getExecutor() != null) {
            return res.getContext().getExecutor().execute(res.getContext());
        }
        throw new CommandParsingException("Invalid command",res.getReader().markerSince(0));
    }

    public CommandNode getCommand(String name) {
        for (CommandNode cmd : getCommands()) {
            if (literalsEqual(cmd.getName(),name)) {
                return cmd;
            }
        }
        return null;
    }

    public List<CommandNode> getCommands() {
        return getRoot().getChildren();
    }

    public CommandNode getRoot() {
        return root;
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
        return areLiteralsIgnoreCase() ? a.equalsIgnoreCase(b) : a.equals(b);
    }
}
