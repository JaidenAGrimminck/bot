package me.autobot.sim.evolution;

import me.autobot.code.Robot;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.server.WSClient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class EvolutionTracker {
    private static EvolutionTracker instance;

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

    public int getTimeLeft() {
        return (int) (genTime - (System.currentTimeMillis() - currentGenStart));
    }

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

        evoTimer.scheduleAtFixedRate(task, 0, evoTimerInterval);
        System.out.println("[evo] Started evolution timer.");

        genRunning = true;
    }

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

    public void setSpeed(int index, double speed) {
        aiSpeeds[index] = speed;
    }

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

    public void setRotation(int index, Rotation2d rotation) {
        aiRotations[index] = rotation;
    }

    public double[] getSpeeds() {
        return aiSpeeds;
    }

    public Rotation2d[] getRotations() {
        return aiRotations;
    }

    public double getScore(int index) {
        int onGoal = this.onGoal[index];

        int crashFactor = 0;
        if (this.referenceRobots.get(index).inCollision()) {
            crashFactor = 100000;
        }

        Vector2d nextGoalPos = goalSteps[onGoal];
        Vector2d lastGoalPos = onGoal > 0 ? goalSteps[onGoal - 1] : new Vector2d(130, 1870);

        double distanceToGoal = this.referenceRobots.get(index).getPosition().distance(nextGoalPos);
        double totDist = nextGoalPos.distance(lastGoalPos);

        return 1 + onGoal * 10000 + (1.5 - (distanceToGoal / totDist)) * 1000 - crashFactor;
    }

    public double[] getScores() {
        double[] scores = new double[referenceRobots.size()];

        for (int i = 0; i < referenceRobots.size(); i++) {
            scores[i] = getScore(i);
        }

        return scores;
    }

    public void stop(int i) {
        aiSpeeds[i] = 0;
        aiRotations[i] = Rotation2d.zero();
    }

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

    public int getGeneration() {
        return generation;
    }

    public void assignWSRef(WSClient wsClient) {
        wsClientRef = wsClient;
    }
}
