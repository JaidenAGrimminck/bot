package me.autobot.sim;

import me.autobot.code.Robot;
import me.autobot.lib.math.coordinates.Int2;

import java.util.ArrayList;

public class Simulation {
    private Robot robot;

    private int[][] obstacles = new int[][] {
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        { 1, 3, 0, 0, 1, 0, 0, 0, 1, 1},
        { 1, 1, 1, 0, 0, 1, 1, 0, 0, 1},
        { 1, 0, 1, 1, 0, 0, 1, 0, 1, 1},
        { 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
        { 1, 0, 1, 1, 1, 0, 1, 0, 0, 1},
        { 1, 0, 0, 1, 0, 1, 1, 1, 0, 1},
        { 1, 1, 2, 0, 0, 0, 1, 0, 0, 1},
        { 1, 0, 0, 0, 1, 0, 0, 0, 1, 1},
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    String ur = "┏";
    String dr = "┗";
    String ul = "┓";
    String dl = "┛";

    String st = "━";

    String ud = "┃";

    String pathStr(Int2 from, Int2 to, Int2 next) {
        boolean fromTop = from.y < to.y;
        boolean fromBottom = from.y > to.y;
        boolean fromRight = from.x > to.x;
        boolean fromLeft = from.x < to.x;

        boolean goingUp = next.y > from.y;
        boolean goingDown = next.y < from.y;
        boolean goingRight = next.x > from.x;
        boolean goingLeft = next.x < from.x;

        String not_straight = " o ";

        if (fromTop) {
            //top to bottom
            if (goingDown) {
                return " " + ud + " ";
            } else if (goingRight) { //top to right
                return " " + dr + st;
            } else if (goingLeft) { //top to left
                return st + dl + " ";
            }
        } else if (fromBottom) {
            if (goingUp) { //bottom to top
                return " " + ud + " ";
            } else if (goingRight) { //bottom to right
                return " " + ur + st;
            } else if (goingLeft) { //bottom to left
                return st + ul + " ";
            }
        } else if (fromLeft) {
            if (goingRight) { //left to right
                return st + st + st;
            } else if (goingUp) { //left to top
                return st + dl + " ";
            } else if (goingDown) { //left to bottom
                return st + ul + " ";
            }
        } else if (fromRight) {
            if (goingLeft) { //right to left
                return st + st + st;
            } else if (goingUp) { //right to top
                return " " + dr + st;
            } else if (goingDown) { //right to bottom
                return " " + ur + st;
            }
        }

        System.out.println(fromLeft + " " + fromRight + " " + fromTop + " " + fromBottom);
        System.out.println(goingLeft + " " + goingRight + " " + goingUp + " " + goingDown);

        if (fromTop && goingUp) {
            return " " + ud + " ";
        }

        return not_straight;
    }

    Int2 end = new Int2(1,1);

    private void printObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            for (int j = 0; j < obstacles[i].length; j++) {
                String l = "   ";

                int o = obstacles[i][j];

                if (o == 1) {
                    l = "███";
                } else if (o == 2) {
                    l = " @ ";
                } else if (o == 3) {
                    l = " X ";
                } else if (o == 4) {
                    l = " o ";
                }
                
                System.out.print(l);
            }
            System.out.println();
        }
    }

    private void pathfind() {
        ArrayList<Int2> openlist = new ArrayList<>();
        ArrayList<Int2> closedList = new ArrayList<>();

        Int2 start = new Int2(1, 1);

        for (int y = 0; y < obstacles.length; y++) {
            for (int x = 0; x < obstacles[y].length; x++) {
                if (obstacles[y][x] == 2) {
                    start = new Int2(x, y);
                }
            }
        }

        start.properties.put("f", 0d);
        start.properties.put("g", 0d);
        start.properties.put("h", 0d);

        openlist.add(start);

        while (!openlist.isEmpty()) {
            //get node with least f
            Int2 current = openlist.get(0);

            int indx = 0;
            int findx = 0;
            for (Int2 node : openlist) {
                if ((double) node.properties.get("f") < (double) current.properties.get("f")) {
                    current = node;
                    findx = indx;
                }
                indx++;
            }

            System.out.println("length of openlist: " + openlist.size());

            openlist.remove(findx);

            System.out.println("length of openlist: " + openlist.size());

            closedList.add(current);

            if (current.equals(end)) {
                break;
            }

            Int2[] children = new Int2[] {
                new Int2(current.x + 1, current.y),
                new Int2(current.x - 1, current.y),
                new Int2(current.x, current.y + 1),
                new Int2(current.x, current.y - 1)
            };

            for (Int2 child : children) {
                //check if child is in closed list
                if (child.inList(closedList) || child.inList(openlist)) {
                    continue;
                }

                if (child.x < 0 || child.y < 0 || child.x >= obstacles[0].length || child.y >= obstacles.length) {
                    continue;
                }

                //check if child is an obstacle
                if (obstacles[child.y][child.x] == 1) {
                    continue;
                }

                //calculate f
                double g = (double) current.properties.get("g") + 1;
                double h = child.distancesq(end);

                double dx1 = child.x - end.x;
                double dy1 = child.y - end.y;
                double dx2 = start.x - end.x;
                double dy2 = start.y - end.y;

                double cross = Math.abs(dx1 * dy2 - dx2 * dy1);
                h = h + cross * 0.001;

                double f = g + h;

                child.properties.put("f", f);
                child.properties.put("g", g);
                child.properties.put("h", h);

                boolean skip = false;

                for (Int2 node : openlist) {
                    if (node.equals(child) && (double) node.properties.get("f") < f) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }
                System.out.println("Adding " + child.toString() + " to openlist...");
                openlist.add(child);
            }

            System.out.println("Searching " + current.toString() + "...");
        }

        if (closedList.contains(end)) {
            System.out.println("Path found!");
        } else {
            System.out.println("Path not found!");
        }

        String[][] stringPath = new String[obstacles.length][obstacles[0].length];

        //then show most efficient path
        for (int i = 0; i < obstacles.length; i++) {
            for (int j = 0; j < obstacles[i].length; j++) {
                String l = "   ";

                int o = obstacles[i][j];

                if (o == 1) {
                    l = "███";
                } else if (o == 2) {
                    l = " @ ";
                } else if (o == 3) {
                    l = " X ";
                }

                stringPath[i][j] = l;
            }
        }

        //find out most optimal path from the closed list, starting from the end
        //to eliminate any unnecessary paths
        Int2 current = end;

        current.properties.put("f", 0d);
        current.properties.put("g", 0d);
        current.properties.put("h", 0d);

        Int2 prelast = end;
        Int2 last = end;

        while (!current.equals(start)) {
            Int2[] children = new Int2[] {
                new Int2(current.x + 1, current.y),
                new Int2(current.x - 1, current.y),
                new Int2(current.x, current.y + 1),
                new Int2(current.x, current.y - 1)
            };

            Int2 newCurrent = null;

            for (Int2 child : children) {
                Int2 getChildAtLoc = child.getInList(closedList);
                if (getChildAtLoc != null) {
                    child = getChildAtLoc;

                    if (current.equals(start)) {
                        newCurrent = child;
                        break;
                    }

                    if ((double) child.properties.get("g") < (double) current.properties.get("g")) {
                        newCurrent = child;
                        break;
                    }
                }
            }

            if (newCurrent == null) {
                for (Int2 child : children) {
                    Int2 getChildAtLoc = child.getInList(closedList);
                    if (getChildAtLoc != null) {
                        child = getChildAtLoc;

                        newCurrent = child;
                    }
                }
            }

            if (!current.equals(end) && !last.equals(end)) {
                stringPath[last.y][last.x] = pathStr(prelast, last, current);
            }


            prelast = last;
            last = current;
            current = newCurrent;

            System.out.println("Path: " + current.toString());
        }

        stringPath[last.y][last.x] = pathStr(prelast, last, current);


        for (String[] row : stringPath) {
            for (String cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    public static void main() {
        Simulation sim = new Simulation();

        sim.printObstacles();

        sim.pathfind();
    }
} 
