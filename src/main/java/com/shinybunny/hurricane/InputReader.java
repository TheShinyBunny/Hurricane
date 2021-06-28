package com.shinybunny.hurricane;

import com.shinybunny.hurricane.util.CommandParsingException;
import com.shinybunny.hurricane.util.InvalidNumberException;
import org.intellij.lang.annotations.RegExp;

import java.util.Optional;
import java.util.function.Function;

public class InputReader {

    private String input;
    private int pos;

    public InputReader(String input) {
        this(input,0);
    }

    public InputReader(String input, int pos) {
        this.input = input;
        this.pos = pos;
    }

    public Optional<String> readOneOf(String... phrases) {
        return readOneOf(true,phrases);
    }

    public Optional<String> readOneOf(boolean ignoreCase, String... phrases) {
        for (String s : phrases) {
            if (readPhrase(s,ignoreCase)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public boolean readPhrase(String phrase, boolean ignoreCase) {
        if (!canRead()) return false;
        int pos = this.pos;
        for (int i = 0; canRead() && i < phrase.length(); i++) {
            if (charEqual(phrase.charAt(i),peek(),ignoreCase)) {
                next();
            } else {
                this.pos = pos;
                return false;
            }
        }
        return true;
    }

    private boolean charEqual(char c1, char c2, boolean ignoreCase) {
        return ignoreCase ? Character.toLowerCase(c1) == Character.toLowerCase(c2) : c1 == c2;
    }

    public int readInteger() throws InvalidNumberException {
        return readNumber(Integer::parseInt);
    }

    public double readDouble() throws InvalidNumberException {
        return readNumber(Double::parseDouble);
    }

    public float readFloat() throws InvalidNumberException {
        return readNumber(Float::parseFloat);
    }

    public long readLong() throws InvalidNumberException {
        return readNumber(Long::parseLong);
    }

    public short readShort() throws InvalidNumberException {
        return readNumber(Short::parseShort);
    }

    public byte readByte() throws InvalidNumberException {
        return readNumber(Byte::parseByte);
    }

    public <N extends Number> N readNumber(Function<String,N> parser) throws InvalidNumberException {
        int pos = this.pos;
        String str = "";
        boolean hasDigits = false;
        if (skip('-')) {
            str += "-";
        }
        str += readPattern("[0-9]*");
        if (!str.isEmpty()) hasDigits = true;
        if (skip('.')) {
            str += ".";
            String dec = readPattern("[0-9]*");
            if (!dec.isEmpty()) hasDigits = true;
            str += dec;
        }
        N number;
        if (!hasDigits) {
            throw new InvalidNumberException("Expected a number",markerSince(pos));
        }
        try {
            number = parser.apply(str);
        } catch (NumberFormatException e) {
            throw new InvalidNumberException("Invalid number " + str,e,markerSince(pos));
        }
        return number;
    }

    private boolean skip(char c) {
        if (peek() == c) {
            next();
            return true;
        }
        return false;
    }

    public String readPattern(@RegExp String regex) {
        String str = "";
        while (canRead()) {
            String curr = str + peek();
            if (!curr.matches(regex)) {
                break;
            }
            str += next();
        }
        return str;
    }

    public String readQuotableString() {
        if (peek() == '"') {
            next();
            return readUntil('"','\\');
        }
        return readWord();
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int steps) {
        if (!canRead(steps + 1)) return 0;
        return input.charAt(pos + steps);
    }

    public char next() {
        return canRead() ? input.charAt(pos++) : 0;
    }

    public boolean canRead() {
        return canRead(1);
    }

    public boolean canRead(int steps) {
        return pos + steps <= input.length();
    }

    public String readUntil(char c, char escapeChar) {
        String s = "";
        while (canRead()) {
            if (peek() == escapeChar) {
                next();
            } else if (peek() == c) break;
            s += next();
        }
        return s;
    }

    public String readWord() {
        return readUntil(' ', (char) 0);
    }

    public void skipSpace() {
        while (canRead() && Character.isWhitespace(peek())) next();
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String readRest() {
        String s = input.substring(pos);
        pos = input.length();
        return s;
    }

    public String getString() {
        return input;
    }

    public SyntaxMarker markerHere() {
        return new SyntaxMarker(this,pos,pos);
    }

    public SyntaxMarker markerSince(int start) {
        return new SyntaxMarker(this,start,pos);
    }

    public InputReader copy() {
        return new InputReader(input,pos);
    }

    @Override
    public String toString() {
        String before = input.substring(0,pos);
        String after = canRead() ? input.substring(pos) : "";
        return before + '|' + after;
    }


    public void expect(char c, String msg) throws CommandParsingException {
        if (canRead() && peek() == c) {
            next();
        } else {
            throw new CommandParsingException(msg,markerHere());
        }
    }

    public Number readAnyNumber() throws InvalidNumberException {
        try {
            return readByte();
        } catch (InvalidNumberException e) {
            try {
                return readShort();
            } catch (InvalidNumberException e2) {
                try {
                    return readInteger();
                } catch (InvalidNumberException e3) {
                    try {
                        return readLong();
                    } catch (InvalidNumberException e4) {
                        try {
                            return readDouble();
                        } catch (InvalidNumberException e5) {
                            return readFloat();
                        }
                    }
                }
            }
        }
    }

    public String readIdentifier() {
        return readPattern("\\w+");
    }
}
