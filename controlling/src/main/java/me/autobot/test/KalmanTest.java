package me.autobot.test;

import me.autobot.lib.math.linalg.LUDecomposition;
import me.autobot.lib.math.linalg.Matrix;
import me.autobot.lib.math.linalg.Vector;
import me.autobot.lib.math.models.KalmanFilter;

/**
 * Class to test the Kalman Filter.
 * */
public class KalmanTest {
    /**
     * Main method to test the Kalman Filter.
     * @param args The arguments to the program.
     * */
    public static void main(String[] args) {
        double[][] matrixData = {
                {4, 3, 0},
                {3, 1, -1},
                {0, -1, 4}
        };

        Matrix matrix = new Matrix(matrixData);
        LUDecomposition luDecomposition = new LUDecomposition(matrix);

        Matrix L = luDecomposition.getL();
        Matrix U = luDecomposition.getU();
        Matrix P = luDecomposition.getP().toMatrix();

        System.out.println("L Matrix:");
        System.out.println(L);

        System.out.println("U Matrix:");
        System.out.println(U);

        System.out.println("P Matrix:");
        System.out.println(P);

        // Initial state: [position, velocity]
        Vector initialState = new Vector(new double[] {0, 0});

        // Initial covariance: uncertainty in position and velocity
        Matrix initialCovariance = Matrix.createIdentityMatrix(2);

        // State transition matrix: simple linear motion
        Matrix transitionMatrix = new Matrix(new double[][] {
                {1, 1}, // position = position + velocity
                {0, 1}  // velocity = velocity (constant velocity model)
        });

        // Control matrix: no external control input for now
        Matrix controlMatrix = new Matrix(new double[][] {
                {0}, // no effect on position
                {0}  // no effect on velocity
        });

        // Measurement matrix: we measure only position
        Matrix measurementMatrix = new Matrix(new double[][] {
                {1, 0} // position only
        });

        // Process noise: small uncertainty in motion
        Matrix processNoise = Matrix.createIdentityMatrix(2).scale(0.1);

        // Measurement noise: small uncertainty in sensor measurements
        Matrix measurementNoise = Matrix.createIdentityMatrix(1).scale(0.2);

        // Create the Kalman Filter
        KalmanFilter kf = new KalmanFilter(
                initialState.toMatrix(),
                initialCovariance,
                transitionMatrix,
                controlMatrix,
                measurementMatrix,
                processNoise,
                measurementNoise
        );

        // Example control input (none in this case)
        Vector controlInput = new Vector(new double[] {0});

        // Simulated measurements (position only)
        double[] measurements = {1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1};

        for (double measurement : measurements) {
            // Predict step
            kf.predict(controlInput);

            // Update step with measurement
            Vector measurementVector = new Vector(new double[] {measurement});
            kf.update(measurementVector);

            // Get the updated state
            Vector state = kf.getState();
            System.out.println("Estimated State: " + state);
        }
    }
}
