package me.autobot.sim;

import me.autobot.lib.math.coordinates.Box2d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Loads maps for the simulator and converts them to Box2d objects.
 * @see me.autobot.lib.math.coordinates.Box2d
 * */
public class MapLoader {

    /**
     * This class should not be instantiated.
     * */
    public MapLoader() {
        throw new IllegalStateException("Cannot instantiate a MapLoader object.");
    }

    /**
     * Loads a map from a file. This is supposed to be a text file where each line represents a row of the map, with 0 representing empty space and 1 representing a box.
     * See the /map directory for examples.
     * @param path The path to the file.
     *             Can be any string.
     *             If the file does not exist, a FileNotFoundException is thrown.
     *             If the file is not a text file, the function may not work as expected.
     * @return A 2D array of integers.
     * @see #mapToObjects(int[][], int)
     * @throws FileNotFoundException If the file does not exist.
     * */
    public static int[][] loadMap(String path) throws FileNotFoundException {
        //open file
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("File not found");
            return null;
        }

        Scanner scanner = new Scanner(new FileInputStream(file));

        int lines = 0;
        int columns = 0;

        while (scanner.hasNext()) {
            String line = scanner.nextLine();

            if (columns == 0) {
                columns = line.length();
            }

            lines++;
        }

        scanner.close();

        scanner = new Scanner(new FileInputStream(file));

        int[][] map = new int[lines][columns];

        for (int i = 0; i < lines; i++) {
            String line = scanner.nextLine();

            for (int j = 0; j < columns; j++) {
                map[i][j] = line.charAt(j) - '0';
            }
        }

        scanner.close();

        return map;
    }

    /**
     * Converts a 2D array of integers (where 1 represents a box and 0 represents empty space) to a list of Box2d objects.
     * @param map The 2D array of integers.
     *            Can be any 2D array of integers, where 1 represents a box and 0 or else represents empty space.
     * @param boxWidth The width of each box. Can be any positive integer.
     *                 If the width is less than or equal to 0, an assertion error is thrown.
     * @return A list of Box2d objects.
     * */
    public static ArrayList<Box2d> mapToObjects(int[][] map, int boxWidth) {
        assert boxWidth > 0;

        ArrayList<Box2d> objects = new ArrayList<>();

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 1) {
                    objects.add(new Box2d(j * boxWidth, i * boxWidth, boxWidth, boxWidth));
                }
            }
        }

        return objects;
    }
}
