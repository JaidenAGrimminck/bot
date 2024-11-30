package me.autobot.lib.math.linalg;

/**
 * A matrix is a rectangular array of numbers, symbols, or expressions, arranged in rows and columns.
 */
public class Matrix {

    /**
     * Creates a new identity matrix with the given size
     * @param size The size of the identity matrix
     * @return The identity matrix
     * */
    public static Matrix createIdentityMatrix(int size) {
        double[][] matrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1;
        }

        return new Matrix(matrix);
    }

    private double[][] matrix;

    /**
     * Creates a new matrix with the given 2D array
     * @param matrix The 2D array to create the matrix with
     * */
    public Matrix(double[][] matrix) {
        int cols = matrix[0].length;

        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i].length != cols) {
                throw new IllegalArgumentException("All rows in the matrix must have the same number of columns!");
            }
        }

        this.matrix = matrix;
    }

    /**
     * Creates a new matrix with the given number of rows and columns
     * @param rows The number of rows in the matrix
     * @param cols The number of columns in the matrix
     * */
    public Matrix(int rows, int cols) {
        matrix = new double[rows][cols];
    }

    /**
     * Gets the matrix as a 2D array
     * @return The matrix as a 2D array
     * */
    public double[][] getMatrix() {
        return matrix;
    }

    /**
     * Gets the number of rows in the matrix
     * @return The number of rows in the matrix
     * */
    public int getRows() {
        return matrix.length;
    }

    /**
     * Gets the number of columns in the matrix
     * @return The number of columns in the matrix
     * */
    public int getCols() {
        return matrix[0].length;
    }

    /**
     * Gets the value at the given row and column
     * @param row The row to get the value from
     * @param col The column to get the value from
     * @return The value at the given row and column
     * */
    public double get(int row, int col) {
        return matrix[row][col];
    }

    /**
     * Sets the value at the given row and column
     * @param row The row to set the value at
     * @param col The column to set the value at
     * @param value The value to set
     * */
    public void set(int row, int col, double value) {
        matrix[row][col] = value;
    }

    /**
     * Swaps two rows in this matrix
     * @param row1 The first row to swap
     *             The row must be within the bounds of the matrix
     * @param row2 The second row to swap
     *             The row must be within the bounds of the matrix
     * */
    public void swapRows(int row1, int row2) {
        double[] temp = matrix[row1];
        matrix[row1] = matrix[row2];
        matrix[row2] = temp;
    }

    /**
     * Adds the matrix to another matrix
     * @param other The matrix to add
     *              The matrix must have the same dimensions as this matrix
     * @return The sum of the two matrices
     * */
    public Matrix add(Matrix other) {
        if (getRows() != other.getRows() || getCols() != other.getCols()) {
            throw new IllegalArgumentException("Matrices must have the same dimensions to add them!");
        }

        double[][] newMatrix = new double[getRows()][getCols()];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                newMatrix[i][j] = matrix[i][j] + other.getMatrix()[i][j];
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Subtracts the matrix from another matrix
     * @param other The matrix to subtract
     * @return The difference of the two matrices
     * */
    public Matrix subtract(Matrix other) {
        if (getRows() != other.getRows() || getCols() != other.getCols()) {
            throw new IllegalArgumentException("Matrices must have the same dimensions to subtract them!");
        }

        double[][] newMatrix = new double[getRows()][getCols()];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                newMatrix[i][j] = matrix[i][j] - other.getMatrix()[i][j];
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Multiplies the matrix by another matrix
     * @param other The matrix to multiply by
     * @return The product of the two matrices
     * */
    public Matrix multiply(Matrix other) {
        if (getCols() != other.getRows()) {
            throw new IllegalArgumentException("The number of columns in the first matrix must be equal to the number of rows in the second matrix to multiply them!");
        }

        double[][] newMatrix = new double[getRows()][other.getCols()];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < other.getCols(); j++) {
                double sum = 0;
                for (int k = 0; k < getCols(); k++) {
                    sum += matrix[i][k] * other.getMatrix()[k][j];
                }
                newMatrix[i][j] = sum;
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Multiplies the matrix by a vector
     * @param v The vector to multiply by
     * @return The product of the matrix and the vector
     * */
    public Matrix multiply(Vector v) {
        if (getCols() != v.size()) {
            throw new IllegalArgumentException("The number of columns in the matrix must be equal to the size of the vector to multiply them!");
        }

        double[][] newMatrix = new double[getRows()][1];

        for (int i = 0; i < getRows(); i++) {
            double sum = 0;
            for (int j = 0; j < getCols(); j++) {
                sum += matrix[i][j] * v.get(j);
            }
            newMatrix[i][0] = sum;
        }

        return new Matrix(newMatrix);
    }

    /**
     * Scales the matrix by a scalar
     * @param scalar The scalar to scale the matrix by
     * @return The scaled matrix
     * */
    public Matrix scale(double scalar) {
        double[][] newMatrix = new double[getRows()][getCols()];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                newMatrix[i][j] = matrix[i][j] * scalar;
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Transposes the matrix
     * @return The transposed matrix
     * */
    public Matrix transpose() {
        double[][] newMatrix = new double[getCols()][getRows()];

        for (int i = 0; i < getCols(); i++) {
            for (int j = 0; j < getRows(); j++) {
                newMatrix[i][j] = matrix[j][i];
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Gets the submatrix of the matrix
     * @param row The row to remove
     * @param col The column to remove
     * @return The submatrix
     * */
    public Matrix submatrix(int row, int col) {
        double[][] newMatrix = new double[getRows() - 1][getCols() - 1];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                if (i != row && j != col) {
                    int newRow = i < row ? i : i - 1;
                    int newCol = j < col ? j : j - 1;
                    newMatrix[newRow][newCol] = matrix[i][j];
                }
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Gets the determinant of the matrix
     * @return The determinant of the matrix
     * */
    public double determinant() {
        if (getRows() != getCols()) {
            throw new IllegalArgumentException("The matrix must be square to calculate the determinant!");
        }

        if (getRows() == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        double det = 0;
        for (int i = 0; i < getCols(); i++) {
            det += Math.pow(-1, i) * matrix[0][i] * submatrix(0, i).determinant();
        }

        return det;
    }

    /**
     * Inverts the matrix
     * @return The inverted matrix
     * */
    public Matrix inverse() {
        if (getRows() != getCols()) {
            throw new IllegalArgumentException("The matrix must be square to calculate the inverse!");
        }

        double det = determinant();
        if (det == 0) {
            throw new IllegalArgumentException("The matrix is singular and cannot be inverted!");
        }

        double[][] newMatrix = new double[getRows()][getCols()];

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                newMatrix[i][j] = Math.pow(-1, i + j) * submatrix(j, i).determinant() / det;
            }
        }

        return new Matrix(newMatrix);
    }

    /**
     * Gets the dimension of the matrix
     * @return The dimension of the matrix
     * */
    public int dimension() {
        return getRows() * getCols();
    }

    /**
     * Gets the string representation of the matrix
     * @return The string representation of the matrix
     * */
    @Override
    public String toString() {
        return this.toString(2);
    }

    /**
     * Gets the string representation of the matrix
     * @param decimals The number of decimals to round to
     * @return The string representation of the matrix
     * */
    public String toString(int decimals) {
        StringBuilder builder = new StringBuilder();

        int longestNumber = 0;

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                double value = matrix[i][j];
                String valueString = String.valueOf(value);

                if (valueString.length() > longestNumber) {
                    longestNumber = valueString.length();
                }
            }
        }

        builder.append("┏ ");

        for (int i = 0; i < getRows(); i++) {
            if (i > 0 && i < getRows() - 1) {
                builder.append("┃ ");
            } else if (i == getRows() - 1) {
                builder.append("┗ ");
            }
            for (int j = 0; j < getCols(); j++) {
                double value = matrix[i][j];

                value = Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
                StringBuilder valueString = new StringBuilder(String.valueOf(value));

                while (valueString.toString().split("\\.")[1].length() < decimals) {
                    valueString.append("0");
                }

                while (valueString.length() < longestNumber) {
                    valueString.insert(0, " ");
                }

                builder.append(valueString);

                if (j < getCols() - 1) {
                    builder.append("  ");
                }
            }
            if (i < getRows() - 1) {
                if (i == 0) {
                    builder.append(" ┓");
                } else {
                    builder.append(" ┃");
                }
                builder.append("\n");
            }
        }

        builder.append(" ┛");

        return builder.toString();
    }
}
