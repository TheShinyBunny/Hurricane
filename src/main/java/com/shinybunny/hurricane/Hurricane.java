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
 * <p>
 * To register commands, call {@link #register(Object)} with a container class for multiple method commands,
 * or call {@link #register(CustomCommand)} with a built command instance.
 */
public class Hurricane extends CustomDataHolder {

    private List<ParamAnnotationAdapter<?>> paramAnnotationAdapters = new ArrayList<>();
    private List<MethodAnnotationAdapter<?>> methodAnnotationAdapters = new ArrayList<>();
    private List<ArgumentAdapter<?>> argumentAdapters = new ArrayList<>();

    private boolean literalsIgnoreCase = true;
    private boolean allowMultiSpaces = true;

    private Predicate<CustomCommand> commandConsumer;
    private Consumer<String> logger;

    protected List<CustomCommand> registeredCommands = new ArrayList<>();

    private String defaultNoPermsMessage;

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
     * @param consumer The callback, with the registered {@link CustomCommand} as the parameter. Returning false will cancel the command registration.
     */
    public void onCommandRegistered(Predicate<CustomCommand> consumer) {
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
        for (int i = 0; i < m.getParameterCount(); i++) {
            Parameter p = m.getParameters()[i];
            ParameterArgument arg = new ParameterArgument(p,i);
            arg.postInit(ctx);
            cmd.addArgument(arg);
        }
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
        addArgumentAdapter(Character.class, InputReader::next);
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
            createFromContainer(c);
        }
    }

    protected void createFromContainer(CommandContainer container) {
        Class<?> cls = container.getContainingClass();
        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                CommandRegisteringContext ctx = new CommandRegisteringContext(this,container,Utils.getName(m));
                CustomCommand cmd = createFromMethod(ctx,m,container);
                register(cmd,ctx);
            }
        }
        for (Class<?> sc : cls.getDeclaredClasses()) {
            if (sc.isAnnotationPresent(Command.class)) {
                register(createTree(new CommandContainer(this,sc),sc.getAnnotation(Command.class)));
            }
        }
    }



    /**
     * Registers a single command to the command tree.
     * <p>
     *     If there is a {@link #onCommandRegistered(Predicate) command registered} consumer set,
     *     Will only register the command if that predicate returned <code>true</code>.
     * </p>
     */
    public void register(CustomCommand cmd) {
        register(cmd,new CommandRegisteringContext(this,null,cmd.getName()));
    }

    public void register(CustomCommand cmd, CommandRegisteringContext ctx) {
        cmd.onRegister(ctx);
        ctx.printErrors();
        if (!ctx.isCancelled()) {
            if (commandConsumer == null || commandConsumer.test(cmd)) {
                log("Added command: " + cmd);
                registeredCommands.add(cmd);
            }
        }
    }

    public <T extends CustomCommand> T register(CommandBuilder<T> builder) {
        CommandRegisteringContext ctx = new CommandRegisteringContext(this,null,builder.getName());
        T cmd = builder.build(ctx);
        register(cmd,ctx);
        return cmd;
    }

    public TreeCommand createTree(CommandContainer container, Command settings) {
        Class<?> cls = container.getContainingClass();
        TreeCommand cmd = new TreeCommand(container.getName(settings));
        cmd.description(settings.desc());
        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                CommandRegisteringContext ctx = new CommandRegisteringContext(this, container, Utils.getName(m));
                CustomCommand sc = createFromMethod(ctx, m, container);
                sc.onRegister(ctx);
                ctx.printErrors();
                if (!ctx.isCancelled()) {
                    cmd.addSubCommand(sc);
                }
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
     * @return An object representing the compiled parsing results, to be saved for later or passed to {@link #execute(CommandExecutionContext)}.
     */
    public CommandExecutionContext parse(CommandSender sender, String input) throws CommandParsingException {
        log("parsing command: " + input);
        InputReader reader = new InputReader(input);
        CommandExecutionContext ctx = new CommandExecutionContext(this,sender,reader);
        for (CustomCommand cmd : registeredCommands) {
            Optional<String> opt = reader.readOneOf(cmd.getNames().toArray(new String[0]));
            if (reader.canRead() && reader.peek() != ' ') continue;
            if (opt.isPresent()) {
                if (cmd.canUse(sender)) {
                    cmd.parse(reader, ctx);
                    return ctx;
                } else {
                    throw new CommandParsingException(cmd.getNoPermsMessage());
                }
            }
        }
        throw new CommandParsingException("Unknown command /" + reader.readWord());
        //return parseNodes(getRoot(), reader, ctx);
    }

    /*private ParseResult parseNodes(CommandNode node, InputReader originalReader, CommandExecutionContext builder) {
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
    }*/

    /**
     * Parses and executes a command input.
     * @param sender The source of execution
     * @param input The command input line to parse
     * @return A {@link CommandResult} with info about the results of running the command.
     * @throws CommandParsingException When an error occurs while parsing the command input. Can be caught and sent to the user to inform about syntax errors.
     * @throws CommandFailedException When an <b>unexpected</b> error occurs while executing the command.
     */
    public CommandResult<?> execute(CommandSender sender, String input) throws CommandParsingException, CommandFailedException {
        CommandExecutionContext res = parse(sender,input);
        return execute(res);
    }

    /**
     * Executes a command from a {@link CommandExecutionContext} object.
     * @param ctx The context object returned from {@link #parse(CommandSender, String)}
     * @return A {@link CommandResult} with info about the results of running the command.
     * @throws CommandParsingException When the parse results have an error it will throw it
     * @throws CommandFailedException When an <b>unexpected</b> error occurs while executing the command.
     */
    public CommandResult<?> execute(CommandExecutionContext ctx) throws CommandParsingException, CommandFailedException {
        log("executing command /" + ctx.getReader().getString());
        if (ctx.getExecutor() != null) {
            return ctx.getExecutor().execute(ctx);
        }
        throw new CommandParsingException("Invalid command",ctx.getReader().markerSince(0));
    }

    public CustomCommand getCommand(String name) {
        for (CustomCommand cmd : getCommands()) {
            if (cmd.nameMatches(name)) {
                return cmd;
            }
        }
        return null;
    }

    public List<CustomCommand> getCommands() {
        return registeredCommands;
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

    public String getDefaultNoPermsMessage() {
        return defaultNoPermsMessage;
    }

    /**
     * Changes the default message sent when the {@link CommandSender} has no permissions to execute the command he was attempting to run.
     * @param defaultNoPermsMessage The message to be used.
     */
    public void setDefaultNoPermsMessage(String defaultNoPermsMessage) {
        this.defaultNoPermsMessage = defaultNoPermsMessage;
    }
}
