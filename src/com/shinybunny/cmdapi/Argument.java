package com.shinybunny.cmdapi;

import com.shinybunny.cmdapi.annotations.Arg;
import com.shinybunny.cmdapi.arguments.ArgumentAdapter;
import com.shinybunny.cmdapi.tree.CommandNode;
import com.shinybunny.cmdapi.tree.MethodCommand;
import com.shinybunny.cmdapi.tree.ParameterArgument;
import com.shinybunny.cmdapi.tree.ParsedArgument;
import com.shinybunny.cmdapi.util.*;

import java.lang.annotation.Annotation;

/**
 * A class representing an argument in a command.
 * <br/><br/>
 * An argument can be either syntax or non-syntax.
 * Syntax arguments are used as part of the command's input syntax, to translate the input string into the type of the argument.
 * <br/>
 * Non-Syntax are used mainly for {@link MethodCommand}s, for additional parameters in a method that do not take part of the input parsing,
 * and are just a convenient way to pass data to the command implementation such as the {@link CommandSender sender}, the original input, the {@link CommandExecutionContext execution context}, etc.
 *
 */
public class Argument extends CommandNode {

    private final Class<?> type;
    private ArgumentAdapter<?> adapter;
    private boolean syntax = true;
    private boolean required = true;
    private SuggestionProvider suggestionProvider;
    private String description;

    public Argument(String name, Class<?> type) {
        super(name);
        this.type = CommandAPI.getPrimitiveWrapper(type);
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
    public boolean instanceOf(Class<?> cls) {
        return cls.isAssignableFrom(type);
    }

    public void parse(InputReader reader, CommandExecutionContext ctx) throws CommandParsingException {
        Object obj = getDefault(ctx);
        int start = reader.getPos();
        if (reader.canRead()) {
            try {
                obj = adapter.parse(reader, ctx, this);
            } catch (CommandParsingException e) {
                if (e.getMarker() == null) {
                    throw new CommandParsingException(e.getMessage(),e.getCause(),reader.markerSince(start));
                } else {
                    throw e;
                }
            }
        }
        try {
            obj = modify(obj, ctx);
        } catch (Exception e) {
            throw new CommandParsingException(e.getMessage());
        }
        ParsedArgument parsed = new ParsedArgument(this,obj);
        ctx.withArgument(name,parsed);
    }

    public Object modify(Object obj, CommandExecutionContext ctx) throws Exception {
        return obj;
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
        return "Argument(" + type.getSimpleName() + " " + name + ",required=" + required + ",syntax=" + syntax + ")";
    }

    public String getSignature() {
        return (required ? "" : "[") + "<" + name + ": " + type.getSimpleName() + ">" + (required ? "" : "]");
    }

    public Object getDefault(CommandExecutionContext ctx) {
        return adapter.getDefault(ctx);
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

    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return description;
    }


}
