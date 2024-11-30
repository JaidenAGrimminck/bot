package me.autobot.lib.math.models;

import me.autobot.lib.math.linalg.LUDecomposition;
import me.autobot.lib.math.linalg.Matrix;
import me.autobot.lib.math.linalg.Vector;

/**
 * A Kalman Filter is a mathematical model that uses a series of measurements observed over time, containing statistical noise and other inaccuracies, and produces estimates of unknown variables that tend to be more accurate than those based on a single measurement alone, by estimating a joint probability distribution over the variables for each timeframe.
 * */
public class KalmanFilter {
    private Matrix state; // state vector, x
    private Matrix stateCovariance; // state covariance, P

    private Matrix transition; // transition matrix, A
    private Matrix control; // control matrix, B
    private Matrix measurement; // measurement matrix, H

    private Matrix processNoise; // process noise covariance, Q
    private Matrix measurementNoise; // measurement noise covariance, R


    /**
     * Creates a new Kalman Filter with the given state, state covariance, transition, control, measurement, process noise, and measurement noise.
     * @param state The state vector, x.
     * @param stateCovariance The state covariance, P.
     * @param transition The transition matrix, A.
     * @param control The control matrix, B.
     * @param measurement The measurement matrix, H.
     * @param processNoise The process noise covariance, Q.
     * @param measurementNoise The measurement noise covariance, R.
     * */
    public KalmanFilter(Matrix state, Matrix stateCovariance, Matrix transition, Matrix control, Matrix measurement, Matrix processNoise, Matrix measurementNoise) {
        this.state = state;
        this.stateCovariance = stateCovariance;
        this.transition = transition;
        this.control = control;
        this.measurement = measurement;
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
    }

    /**
     * Predicts the next state of the system given the control input.
     * @param controlInput The control input.
     * */
    public void predict(Vector controlInput) {
        state = transition.multiply(state).add(control.multiply(controlInput));

        stateCovariance = transition
                .multiply(stateCovariance)
                .multiply(transition.transpose())
                .add(processNoise);
    }

    /**
     * Updates the state of the system given the measurement input.
     * @param measurementInput The measurement input.
     * */
    public void update(Vector measurementInput) {
        Vector innovation = measurementInput.subtract(measurement.multiply(state));

        Matrix innovationCovariance = measurement
                .multiply(stateCovariance)
                .multiply(measurement.transpose())
                .add(measurementNoise);

        LUDecomposition luDecomposition = new LUDecomposition(innovationCovariance);

        Matrix invertedMatrix = luDecomposition.invert();

        Matrix kalmanGain = stateCovariance
                .multiply(measurement.transpose())
                .multiply(invertedMatrix);

        // Update state: x = x + K * y
        state = state.add(kalmanGain.multiply(innovation));

        // Update covariance: P = (I - K * H) * P
        Matrix identity = Matrix.createIdentityMatrix(state.dimension());
        stateCovariance = identity
                .subtract(kalmanGain.multiply(measurement))
                .multiply(stateCovariance);
    }

    /**
     * Returns the current state of the system.
     * @return The current state of the system.
     * */
    public Vector getState() {
        Vector state = new Vector(this.state.dimension());

        for (int i = 0; i < this.state.dimension(); i++) {
            state.set(i, this.state.get(i, 0));
        }

        return state;
    }
}
