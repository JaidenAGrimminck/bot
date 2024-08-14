package me.autobot.lib.math.coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Int2 {
    public int x;
    public int y;

    public static Int2 zero() {
        return new Int2(0, 0);
    }

    public HashMap<String, Object> properties = new HashMap<>();

    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Int2 other) {
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
    }

    public double distancesq(Int2 other) {
        return Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2);
    }

    public boolean inList(ArrayList<Int2> list) {
        for (Int2 i : list) {
            if (i.x == x && i.y == y) {
                return true;
            }
        }
        return false;
    }


    public Int2 add(Int2 other) {
        return new Int2(x + other.x, y + other.y);
    }

    public Int2 getInList(ArrayList<Int2> list) {
        for (Int2 i : list) {
            if (i.x == x && i.y == y) {
                return i;
            }
        }
        return null;
    }

    public Vector2d toVector2d() {
        return new Vector2d(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Int2 other) {
            return other.x == x && other.y == y;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
