package com.shinybunny.hurricane.tree;

import com.shinybunny.hurricane.*;
import com.shinybunny.hurricane.arguments.ArgumentAdapter;
import com.shinybunny.hurricane.util.*;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A class representing an argument in a command.
 * <p>
 * An argument can be either syntax or non-syntax.
 * Syntax arguments are parsed from the command's input, to translate the input string into the type of the argument.
 * <p>
 * Non-Syntax are used mainly for {@link MethodCommand}s, for additional parameters in a method that do not take part of the input parsing,
 * and are just a convenient way to pass data to the command implementation such as the {@link CommandSender sender}, the original input, the {@link CommandExecutionContext execution context}, etc.
 *
 */
public class Argument extends CustomDataHolder {

    protected String name;
    protected String description = "";
    protected String typeHint;
    private final Class<?> type;
    private ArgumentAdapter<?> adapter;
    private boolean syntax = true;
    private boolean required = true;
    private boolean needsSpaceAfter = true;
    private SuggestionProvider suggestionProvider;

    public Argument(String name, Class<?> type) {
        this.name = name;
        this.type = Hurricane.getPrimitiveWrapper(type);
        this.typeHint = type.getSimpleName();
    }

    public static Argument of(String name, Class<?> type) {
        return new Argument(name, type);
    }

    /**
     * Initializes the adapters of this argument. This method may only be called once, and shouldn't be called by users of the API.
     * @param ctx The current command registering context
     */
    public void postInit(CommandRegisteringContext ctx) {
        if (adapter != null) return;
        adapter = ctx.getApi().getArgumentAdapter(this,ctx);
        suggestionProvider = adapter;
        if (adapter == null) {
            ctx.addError("No argument adapter found for " + type);
            adapter = new ArgumentAdapter<Object>() {
                @Override
                public Class<Object> getType() {
                    return Object.class;
                }

                @Override
                public void init(Argument container, CommandRegisteringContext ctx) {

                }

                @Override
                public Object parse(InputReader reader, CommandExecutionContext ctx, Argument argument) throws CommandParsingException {
                    return null;
                }

                @Override
                public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender, Argument arg) {

                }
            };
        }
        adapter.init(this,ctx);
    }

    /**
     * Changes the name of the argument.
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Gets the name of this argument. This name is mainly used when providing error messages, when the input is invalid.
     * <br>
     * Parameter arguments use the parameter's name by default, and this can be modified using the builtin {@link com.shinybunny.hurricane.annotations.Arg} annotation.
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the description of the argument
     * @param description The new description
     */
    public Argument description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the description of this argument. Can be used for providing command help.
     */
    public String getDescription() {
        return description;
    }

    public Argument typeHint(String typeHint) {
        this.typeHint = typeHint;
        return this;
    }

    public String getTypeHint() {
        return typeHint;
    }

    /**
     * Get the argument adapter this argument uses.
     * @return The argument adapter, used for defining the parsing of this argument.
     */
    public ArgumentAdapter<?> getAdapter() {
        return adapter;
    }

    /**
     * Sets this argument to be part/not part of the command syntax.
     * @param syntax Whether to make this argument part of the syntax
     */
    public void setSyntax(boolean syntax) {
        this.syntax = syntax;
    }

    public void setSuggestionProvider(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    /**
     * Whether the argument is part of the command syntax. Part of the syntax means its value is parsed from the command input.
     * Non syntax arguments are used to pass additional data to the command execution
     * @return Whether this argument is part of the syntax
     */
    public boolean isSyntax() {
        return syntax;
    }

    /**
     * Checks whether the given class is the same as or the superclass of the argument's type.
     * @param cls The other class in question
     * @return The result of <code>cls.</code>{@link Class#isAssignableFrom(Class) isAssignableFrom}<code>(this.type)</code>
     */
    public boolean typeExtends(Class<?> cls) {
        return cls.isAssignableFrom(type);
    }

    /**
     * Checks whether the given class is the same as or a subclass of the argument's type.
     * @param cls The other class in question
     * @return The result of <code>this.type.</code>{@link Class#isAssignableFrom(Class) isAssignableFrom}<code>(cls)</code>
     */
    public boolean typeExtendedBy(Class<?> cls) {
        return type.isAssignableFrom(cls);
    }

    /**
     * Parses this argument from the command input.
     * If this argument is not part of the syntax, resolves the value from the context.
     * Either way, we also validate the value against the annotations
     */
    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        ctx.getApi().log("parsing argument " + this + " starting with: " + reader.peek());
        Object obj = null;
        int start = reader.getPos();
        if (isSyntax()) {
            if (reader.canRead()) {
                try {
                    obj = adapter.parse(reader, ctx, this);
                } catch (CommandParsingException e) {
                    if (e.getMarker() == null) {
                        throw new CommandParsingException(e.getMessage(), e.getCause(), reader.markerSince(start));
                    } else {
                        throw e;
                    }
                }
            } else if (isRequired()) {
                throw new CommandParsingException("Expected argument " + name);
            }
        }
        if (obj == null) {
            try {
                obj = getDefault(ctx);
            } catch (Exception e) {
                throw new CommandParsingException(e.getMessage());
            }
        }
        try {
            validate(obj,ctx);
        } catch (Exception e) {
            throw new CommandParsingException(e.getMessage());
        }
        ParsedArgument parsed = new ParsedArgument(this,obj);
        ctx.withArgument(name,parsed);
    }

    public void validate(Object obj, CommandExecutionContext ctx) throws Exception {
        if (!type.isInstance(obj)) {
            throw new Exception("Argument " + name + " must be of type " + type.getSimpleName());
        }
    }

    public Object getDefault(CommandExecutionContext ctx) throws Exception {
        return adapter.getDefault(ctx);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "type=" + type +
                ", required=" + required +
                ", name='" + name + '\'' +
                "}";
    }

    public String getSignature() {
        return (required ? "" : "[") + "<" + name + ": " + type.getSimpleName() + ">" + (required ? "" : "]");
    }



    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return false;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    public void suggest(InputReader reader, SuggestionConsumer suggestions, CommandSender sender) {
        if (suggestionProvider != null) {
            suggestionProvider.suggest(reader, suggestions, sender, this);
        }
    }

    public List<String> getSuggestions(InputReader reader, CommandSender sender) {
        SuggestionConsumer consumer = new SuggestionConsumer();
        suggest(reader,consumer,sender);
        return consumer.getResult();
    }

    public void setNeedsSpaceAfter(boolean needsSpaceAfter) {
        this.needsSpaceAfter = needsSpaceAfter;
    }

    public boolean needsSpaceAfter() {
        return needsSpaceAfter;
    }

    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }


}
