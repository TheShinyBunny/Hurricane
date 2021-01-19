package com.shinybunny.hurricane.annotations;

import com.shinybunny.hurricane.Hurricane;
import com.shinybunny.hurricane.CommandExecutionContext;
import com.shinybunny.hurricane.CommandRegisteringContext;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandRegisterFailedException;
import org.jetbrains.annotations.Nullable;

public class NullableAdapter implements ParamAnnotationAdapter<Nullable> {
    @Override
    public Object modify(Object value, Nullable annotation, ParameterArgument argument, CommandExecutionContext ctx) throws Exception {
        return null;
    }

    @Override
    public Class<Nullable> getType() {
        return Nullable.class;
    }

    @Override
    public void init(Nullable instance, ParameterArgument container, CommandRegisteringContext ctx) throws CommandRegisterFailedException {
        container.setRequired(false);
    }

    @Override
    public boolean canApply(Nullable instance, ParameterArgument container, CommandRegisteringContext ctx) {
        return !Hurricane.PRIMITIVES_TO_WRAPPERS.containsValue(container.getType());
    }
}
