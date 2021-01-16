package com.shinybunny.cmdapi;

public class IntRange {

    private int start;
    private int end;

    public IntRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static IntRange at(int pos) {
        return new IntRange(pos,pos);
    }

    public static IntRange encompassing(IntRange a, IntRange b) {
        return new IntRange(Math.min(a.start,b.start),Math.max(a.end,b.end));
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isEmpty() {
        return start == end;
    }
}
