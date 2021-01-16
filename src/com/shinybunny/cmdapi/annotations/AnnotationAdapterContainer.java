package com.shinybunny.cmdapi.annotations;

import com.shinybunny.cmdapi.CommandRegisteringContext;
import com.shinybunny.cmdapi.util.CommandRegisterFailedException;
import com.shinybunny.cmdapi.util.CustomDataHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface AnnotationAdapterContainer<A extends AnnotationAdapter> {

    default List<A> resolveAnnotationAdapters(CommandRegisteringContext ctx) {
        List<A> adapters = new ArrayList<>();
        for (Annotation a : getAnnotatedElement().getAnnotations()) {
            if (a.annotationType() == Command.class) continue;
            A adapter = getAdapterFor(ctx,a.annotationType());
            if (adapter == null) {
                Adapter aa = a.annotationType().getAnnotation(Adapter.class);
                DummyAdapter dummy = a.annotationType().getAnnotation(DummyAdapter.class);
                if (aa == null) {
                    if (dummy == null) {
                        ctx.addError("No adapter found for annotation type " + a.annotationType() + " on " + this);
                        continue;
                    } else {
                        adapter = dummyAdapter(a.annotationType());
                        if (!dummy.flag().isEmpty()) {
                            getDataHolder().addFlag(dummy.flag());
                        }
                    }
                } else {
                    try {
                        adapter = (A) aa.value().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        ctx.addError(new CommandRegisterFailedException(e));
                        continue;
                    }
                }
            }
            if (adapter.canApply(a,this,ctx)) {
                adapters.add(adapter);
            } else {
                ctx.addError("Adapter for " + a.annotationType() + " cannot be applied to " + this);
            }
        }
        Random r = new Random();
        for (int i = 0; i < adapters.size() - 1; i++) {
            A a = adapters.get(i);
            for (int j = i + 1; j < adapters.size(); j++) {
                A b = adapters.get(j);
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
        return adapters;
    }

    A dummyAdapter(Class<? extends Annotation> annotationType);

    A getAdapterFor(CommandRegisteringContext ctx, Class<? extends Annotation> annotationType);

    AnnotatedElement getAnnotatedElement();

    CustomDataHolder getDataHolder();

}
