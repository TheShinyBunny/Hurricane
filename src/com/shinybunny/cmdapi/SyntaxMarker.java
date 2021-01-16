package com.shinybunny.cmdapi;

public class SyntaxMarker {

    private InputReader reader;
    private int start;
    private int end;

    public SyntaxMarker(InputReader reader, int start, int end) {
        this.reader = reader;
        this.start = start;
        this.end = end;
    }

    public InputReader getReader() {
        return reader;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    @Override
    public String toString() {
        String before = reader.getString().substring(Math.max(0,start - 10),start);
        String after = reader.getString().substring(end,Math.min(reader.getString().length(),end + 10));
        return ((start - 10 > 0 ? "..." : "") + before) + (start == end ? "[*]" : reader.getString().substring(start,end)) + (after + (end + 10 < reader.getString().length() - 1 ? "..." : ""));
    }
}
