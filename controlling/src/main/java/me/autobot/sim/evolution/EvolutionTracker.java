package me.autobot.sim.evolution;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.server.WSClient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/***
 * Used to track the evolution of the robots
 * and manage the evolution process
 *
 * TODO: generalize class.
 */
public class EvolutionTracker {
    private static EvolutionTracker instance;

    /**
     * Gets the instance of the EvolutionTracker
     * @return The instance of the EvolutionTracker
     * */
    public EvolutionTracker getInstance() {
        return instance;
    }

    private ArrayList<Robot> referenceRobots;

    private int generation;

    private double[] aiSpeeds = new double[0];
    private Rotation2d[] aiRotations = new Rotation2d[0];

    private int numberOfRobots;

    private boolean genRunning = false;

    final double aiSpeedIncrement = 0.1;
    final Rotation2d aiDirectionIncrement = new Rotation2d((Math.PI / 100) / 50);

    private Timer evoTimer;
    private int evoTimerInterval = 1000 / 20;
    private long currentGenStart = 0;
    private long genTime = 1000 * 5;

    private WSClient wsClientRef;

    private final Vector2d[] goalSteps = new Vector2d[] {
            new Vector2d(
                    540, 160
            ),
            new Vector2d(
                    930, 1670
            ),
            new Vector2d(
                    1670, 170
            ),
            new Vector2d(
                1820, 1720
            )
    };

    private int[] onGoal = new int[0];

    /**
     * Constructor for EvolutionTracker
     * @param referenceRobots The robots to track
     */
    public EvolutionTracker(ArrayList<Robot> referenceRobots) {
        instance = this;

        this.referenceRobots = referenceRobots;
        generation = 0;

        numberOfRobots = referenceRobots.size();

        aiSpeeds = new double[referenceRobots.size()];
        aiRotations = new Rotation2d[referenceRobots.size()];
        onGoal = new int[referenceRobots.size()];

        for (int i = 0; i < numberOfRobots; i++) {
            aiSpeeds[i] = 0;
            aiRotations[i] = Rotation2d.zero();
            onGoal[i] = 0;
        }
    }

    /**
     * Gets how much time is left in a generation.
     * @return The time left in the generation
     * */
    public int getTimeLeft() {
        return (int) (genTime - (System.currentTimeMillis() - currentGenStart));
    }

    /**
     * Starts the evolution process
     * */
    public void start() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int n_crashed = 0;
                for (int i = 0; i < numberOfRobots; i++) {
                    n_crashed += referenceRobots.get(i).inCollision() ? 1 : 0;

                    if (referenceRobots.get(i).getPosition().distance(goalSteps[onGoal[i]]) < 150) {
                        onGoal[i] += 1;
                    }
                }

                if (n_crashed == numberOfRobots) {
                    genRunning = false;

                    System.out.println("[evo] Generation " + generation + " complete");

                    startNewGen();
                }

                if (genRunning) {
                    if (getTimeLeft() < 0) {
                        genRunning = false;

                        System.out.println("[evo] Generation " + generation + " complete");

                        startNewGen();
                    }
                }

                if (wsClientRef != null) {
                    if (!wsClientRef.isOpen()) {
                        for (int i = 0; i < numberOfRobots; i++) {
                            stop(i);
                        }
                    }
                }
            }
        };

        evoTimer = new Timer();

        currentGenStart = System.currentTimeMillis();
        generation = 1;

        for (int i = 0; i < numberOfRobots; i++) {
            stop(i);
        }

        evoTimer.scheduleAtFixedRate(task, 0, evoTimerInterval);
        System.out.println("[evo] Started evolution timer.");

        genRunning = true;
    }

    /**
     * Changes the speed of a robot by an increment
     * @param index The index of the robot
     *              to change the speed of
     * @param increase Whether to increase or decrease the speed
     * */
    public void changeSpeed(int index, boolean increase) {
        if (increase) {
            aiSpeeds[index] += aiSpeedIncrement;
        } else {
            aiSpeeds[index] -= aiSpeedIncrement;
        }

        if (aiSpeeds[index] < 0) {
            aiSpeeds[index] = 0;
        }
    }

    /**
     * Sets the speed of a robot
     * @param index The index of the robot
     *              to change the speed of
     * @param speed The speed to set the robot to
     * */
    public void setSpeed(int index, double speed) {
        aiSpeeds[index] = speed;
    }

    /**
     * Rotates a robot by an increment
     * @param index The index of the robot
     *              to rotate
     * @param clockwise Whether to rotate the robot
     *                 clockwise or counter-clockwise
     * */
    public void rotate(int index, boolean clockwise) {
        if (clockwise) {
            aiRotations[index] = aiRotations[index].rotateBy(aiDirectionIncrement);
        } else {
            aiRotations[index] = aiRotations[index].rotateBy(aiDirectionIncrement.inverse());
        }

        if (Math.abs(aiRotations[index].getRadians()) > Math.PI / 100) {
            aiRotations[index] = Rotation2d.fromRadians(Math.PI / 100 * Math.signum(aiRotations[index].getRadians()));
        }
    }

    /**
     * Sets the rotation of a robot
     * @param index The index of the robot
     *              to rotate
     * @param rotation The rotation to set the robot to
     * */
    public void setRotation(int index, Rotation2d rotation) {
        aiRotations[index] = rotation;
    }

    /**
     * Gets the speeds of the robots
     * @return The speeds of the robots
     * */
    public double[] getSpeeds() {
        return aiSpeeds;
    }

    /**
     * Gets the rotations of the robots
     * @return The rotations of the robots
     * */
    public Rotation2d[] getRotations() {
        return aiRotations;
    }

    /**
     * Gets how well a robot is performing
     * @param index The index of the robot
     * @return The score of the robot
     * */
    public double getScore(int index) {
        int onGoal = this.onGoal[index];

        int crashFactor = 0;
        if (this.referenceRobots.get(index).inCollision()) {
            crashFactor = 1000;
        }

        Vector2d nextGoalPos = goalSteps[onGoal];
        //todo: generalize this.
        Vector2d lastGoalPos = onGoal > 0 ? goalSteps[onGoal - 1] : new Vector2d(130, 1870);

        double distanceToGoal = this.referenceRobots.get(index).getPosition().distance(nextGoalPos);
        double totDist = nextGoalPos.distance(lastGoalPos);

        return 1 + onGoal * 10000 + (1.5 - (distanceToGoal / totDist)) * 1000 - crashFactor;
    }

    /**
     * Gets the scores of the robots
     * @return The scores of the robots
     * */
    public double[] getScores() {
        double[] scores = new double[referenceRobots.size()];

        for (int i = 0; i < referenceRobots.size(); i++) {
            scores[i] = getScore(i);
        }

        return scores;
    }

    /**
     * Stops a robot.
     * @param i The index of the robot to stop.
     */
    public void stop(int i) {
        aiSpeeds[i] = 0;
        aiRotations[i] = Rotation2d.zero();
    }

    /**
     * Starts a new generation
     * */
    public void startNewGen() {
        if (genRunning) {
            return;
        }

        sendScoreInfo();

        generation += 1;

        int i = 0;
        for (Robot robot : referenceRobots) {
            robot.reset();
            stop(i);

            i++;
        }

        currentGenStart = System.currentTimeMillis();
        genTime += 1000;

        genRunning = true;
    }

    /**
     * Sends the score information to the websocket client
     * */
    public void sendScoreInfo() {
        if (wsClientRef == null) {
            return;
        }

        if (!wsClientRef.isOpen()) {
            return;
        }

        byte[] payload = new byte[1 + Double.BYTES * (referenceRobots.size() + 1)];
        payload[0] = (byte) 0xC3;

        ByteBuffer bbuf = ByteBuffer.allocate((referenceRobots.size() + 1) * Double.BYTES);
        bbuf.putDouble(generation);
        Arrays.stream(getScores()).forEach(bbuf::putDouble);

        System.arraycopy(bbuf.array(), 0, payload, 1, bbuf.array().length);

        try {
            wsClientRef.send(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current generation
     * @return The current generation
     * */
    public int getGeneration() {
        return generation;
    }

    /**
     * Assigns a reference to the websocket client to send data to
     * @param wsClient The websocket client to send data to
     * */
    public void assignWSRef(WSClient wsClient) {
        wsClientRef = wsClient;
    }
}
