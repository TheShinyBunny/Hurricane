package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;
import com.shinybunny.hurricane.util.SuggestionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Defines a custom set of suggestions for a parameter argument.
 * You may use both {@link #value()} and {@link #from()} together.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Adapter(Suggest.Adapter.class)
public @interface Suggest {

    /**
     * Define a static array of values to suggest
     */
    String[] value() default {};

    /**
     * A reference to a class implementing the {@link SuggestionProvider} interface. The class must have no constructor or a constructor taking 0 parameters.
     * @return A class implementing {@link SuggestionProvider}
     */
    Class<? extends SuggestionProvider> from() default SuggestionProvider.class;

    class Adapter implements ParamAnnotationAdapter<Suggest> {

        @Override
        public Object modify(Object value, Suggest annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
            return null;
        }

        @Override
        public Class<Suggest> getType() {
            return Suggest.class;
        }

        @Override
        public void init(Suggest instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {
            AtomicReference<SuggestionProvider> provider = new AtomicReference<>();
            if (instance.from() != SuggestionProvider.class && !instance.from().isInterface()) {
                try {
                    provider.set(instance.from().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new CommandRegisterFailedException(e);
                }
            }
            container.setSuggestionProvider(((reader, suggestions, sender, arg) -> {
                if (instance.value().length > 0) {
                    suggestions.suggest(instance.value());
                }
                if (provider.get() != null) {
                    provider.get().suggest(reader,suggestions,sender,arg);
                }
            }));
        }
    }

}
