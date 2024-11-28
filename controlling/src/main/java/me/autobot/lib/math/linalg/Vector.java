package me.autobot.lib.math.linalg;

/**
 * Creates a new vector with the given values.
 * Not to be disturbed with Vector2d or Vector3d, as this is a general vector.
 * */
public class Vector {
    private double[] values;

    /**
     * Creates a new vector with the given values.
     * @param values The values of the vector.
     * */
    public Vector(double[] values) {
        this.values = values;
    }

    /**
     * Creates a new vector with the given size.
     * @param size The size of the vector.
     * */
    public Vector(int size) {
        this.values = new double[size];

        for (int i = 0; i < size; i++) {
            values[i] = 0;
        }
    }

    /**
     * Gets the values of the vector.
     * @return The values of the vector.
     * */
    public double[] getValues() {
        return values;
    }

    /**
     * Gets the value at the given index.
     * @param index The index to get the value for.
     * @return The value at the given index.
     * */
    public double get(int index) {
        return values[index];
    }

    /**
     * Sets the value at the given index.
     * */
    public void set(int index, double value) {
        values[index] = value;
    }

    /**
     * Swaps the values at the given indices.
     * */
    public void swap(int index1, int index2) {
        double temp = values[index1];
        values[index1] = values[index2];
        values[index2] = temp;
    }

    /**
     * Gets the size of the vector.
     * */
    public int size() {
        return values.length;
    }

    /**
     * Adds the given vector to this vector.
     * @param vector The vector to add.
     * @return The sum of the two vectors.
     * */
    public Vector add(Vector vector) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = values[i] + vector.get(i);
        }
        return new Vector(newValues);
    }

    /**
     * Subtracts the given vector from this vector.
     * @param vector The vector to subtract.
     * @return The difference of the two vectors.
     * */
    public Vector subtract(Vector vector) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = values[i] - vector.get(i);
        }
        return new Vector(newValues);
    }

    /**
     * Subtracts the given matrix from this vector.
     * @param matrix The matrix to subtract.
     * @return The difference of the vector and the matrix.
     * */
    public Vector subtract(Matrix matrix) {
        if (matrix.getRows() != 1) {
            throw new IllegalArgumentException("Matrix must have 1 row.");
        }
        if (matrix.getCols() != values.length) {
            throw new IllegalArgumentException("Matrix must have the same number of columns as the vector has values.");
        }
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = values[i] - matrix.get(0, i);
        }
        return new Vector(newValues);
    }

    /**
     * Converts the vector to a matrix.
     * */
    public Matrix toMatrix() {
        double[][] newValues = new double[values.length][1];
        for (int i = 0; i < values.length; i++) {
            newValues[i][0] = values[i];
        }
        return new Matrix(newValues);
    }

    /**
     * Returns the vector as a string.
     * */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("⟨");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if (i != values.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("⟩");
        return builder.toString();
    }
}
