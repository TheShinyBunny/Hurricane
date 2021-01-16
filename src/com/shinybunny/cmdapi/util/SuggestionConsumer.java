package com.shinybunny.cmdapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuggestionConsumer {

    private List<String> suggestions;

    public SuggestionConsumer() {
        suggestions = new ArrayList<>();
    }

    public void suggest(String str) {
        suggestions.add(str);
    }

    public void suggest(String... strs) {
        for (String s : strs) {
            suggest(s);
        }
    }

    public void suggestEnum(Class<? extends Enum<?>> enumClass) {
        Object[] consts = enumClass.getEnumConstants();
        suggest(Arrays.stream(consts).map(String::valueOf).toArray(String[]::new));
    }

    public List<String> getResult() {
        return suggestions;
    }

}
