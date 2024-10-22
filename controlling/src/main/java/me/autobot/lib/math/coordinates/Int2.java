package me.autobot.lib.math.coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * A class that represents a 2D integer coordinate.
 * */
public class Int2 {
    /** The x coordinate value of the Int2 object. */
    public int x;

    /** The y coordinate value of the Int2 object. */
    public int y;

    /**
     * Creates a blank Int2 object with the values of 0, 0.
     * @return A new Int2 object with the values of 0, 0.
     * */
    public static Int2 zero() {
        return new Int2(0, 0);
    }

    /** The properties of the Int2 object, such as flags, identities, etc. */
    public HashMap<String, Object> properties = new HashMap<>();

    /**
     * Creates a new Int2 object with the given x and y values.
     * @param x The x value of the Int2 object.
     * @param y The y value of the Int2 object.
     * */
    public Int2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the distance between this Int2 object and another Int2 object.
     * @param other The other Int2 object.
     * @return The distance between the two Int2 objects.
     */
    public double distance(Int2 other) {
        return Math.sqrt(distancesq(other));
    }

    /**
     * Calculates the square of the distance between this Int2 object and another Int2 object.
     * Equivalent to hypotenuse^2.
     * @param other The other Int2 object.
     * @return The square of the distance between the two Int2 objects.
     */
    public double distancesq(Int2 other) {
        return Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2);
    }

    /**
     * Checks if this Int2 object is in a passed list of Int2 objects.
     * @param list The list of Int2 objects to check.
     * @return True if this Int2 object is in the list, false otherwise.
     * */
    public boolean inList(ArrayList<Int2> list) {
        for (Int2 i : list) {
            if (i.x == x && i.y == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new Int2 object that is the sum of this Int2 object and another Int2 object.
     * @param other The other Int2 object.
     * @return A new Int2 object that is the sum of this Int2 object and the other Int2 object.
     * */
    public Int2 add(Int2 other) {
        return new Int2(x + other.x, y + other.y);
    }

    /**
     * Finds and gets the Int2 object in a list of Int2 objects that is equal to this Int2 object.
     * @param list The list of Int2 objects to check.
     * @return The Int2 object in the list that is equal to this Int2 object. May be null if not found.
     * */
    public Int2 getInList(ArrayList<Int2> list) {
        for (Int2 i : list) {
            if (i.x == x && i.y == y) {
                return i;
            }
        }
        return null;
    }

    /**
     * Converts this Int2 object to a Vector2d object.
     * @return A new Vector2d object with the same x and y values as this Int2 object.
     * */
    public Vector2d toVector2d() {
        return new Vector2d(x, y);
    }

    /**
     * Checks if this Int2 object is equal to another object via comparing x and y values.
     * @param obj The other object.
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Int2 other) {
            return other.x == x && other.y == y;
        }
        return false;
    }

    /**
     * Converts this Int2 object to a string.
     * @return A string representation of this Int2 object.
     * */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
