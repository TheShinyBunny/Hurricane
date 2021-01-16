package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandAPI;
import com.shinybunny.cmdapi.CommandExecutionContext;
import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.tree.ParameterArgument;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;
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
        return !CommandAPI.PRIMITIVES_TO_WRAPPERS.containsValue(container.getType());
    }
}
