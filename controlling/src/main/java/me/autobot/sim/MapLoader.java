package me.autobot.sim;

import me.autobot.lib.math.coordinates.Box2d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MapLoader {

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

    public static ArrayList<Box2d> mapToObjects(int[][] map, int boxWidth) {
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
