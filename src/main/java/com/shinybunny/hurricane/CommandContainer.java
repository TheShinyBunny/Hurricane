package com.shinybunny.hurricane;

import com.shinybunny.hurricane.annotations.Command;
import com.shinybunny.hurricane.annotations.InstanceCtor;
import com.shinybunny.hurricane.tree.ParameterArgument;
import com.shinybunny.hurricane.util.CommandFailedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class CommandContainer {

    private Class<?> clazz;
    private Object instance;
    private Constructor instanceCtor;
    private List<ParameterArgument> instanceCtorArgs;

    public CommandContainer(Hurricane api, Object obj) {
        this.instance = obj instanceof Class ? null : obj;
        this.clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        if (instance == null) {
            for (Constructor c : clazz.getConstructors()) {
                if (c.isAnnotationPresent(InstanceCtor.class)) {
                    instanceCtor = c;
                    instanceCtorArgs = new ArrayList<>();
                    CommandRegisteringContext ctx = new CommandRegisteringContext(api, this, "");
                    for (int i = 0; i < c.getParameterCount(); i++) {
                        Parameter p = c.getParameters()[i];
                        ParameterArgument arg = new ParameterArgument(p, i);
                        arg.postInit(ctx);
                        instanceCtorArgs.add(arg);
                    }
                    break;
                }
            }
        }
    }

    public Object createInstanceFromCtor(CommandExecutionContext ctx) throws Exception {
        if (instanceCtor != null) {
            List<Object> args = new ArrayList<>();
            for (ParameterArgument a : instanceCtorArgs) {
                try {
                    Object obj = a.getDefault(ctx);
                    args.add(obj);
                } catch (Exception e) {
                    throw new CommandFailedException(e);
                }
            }
            return instanceCtor.newInstance(args.toArray());
        }
        return clazz.newInstance();
    }

    public Object getInstance(CommandExecutionContext ctx) throws CommandFailedException {
        try {
            return instance == null ? createInstanceFromCtor(ctx) : instance;
        } catch (CommandFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandFailedException(e);
        }
    }


    public Class<?> getContainingClass() {
        return clazz;
    }

    public String getName(Command settings) {
        if (settings.value().isEmpty()) {
            return clazz.getSimpleName().toLowerCase().replace("command", "");
        }
        return settings.value();
    }

    public void setInstance(Object obj) {
        instance = obj;
    }
}
