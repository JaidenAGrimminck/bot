package me.autobot.lib.math.linalg;

/**
 * A LU Decomposition is a way of decomposing a matrix into the product of a lower triangular matrix and an upper triangular matrix.
 * */
public class LUDecomposition {
    private Matrix L; // Lower triangular matrix
    private Matrix U; // Upper triangular matrix

    private Vector P; // Permutation vector

    private boolean singular = false;

    /**
     * Creates a new LU Decomposition with the given lower triangular matrix and upper triangular matrix.
     * @param L The lower triangular matrix.
     * @param U The upper triangular matrix.
     * */
    public LUDecomposition(Matrix L, Matrix U) {
        this.L = L;
        this.U = U;
    }

    /**
     * Creates a new LU Decomposition with the given matrix.
     * @param a The matrix to decompose.
     */
    public LUDecomposition(Matrix a) {
        int n = a.getRows();

        L = new Matrix(n, n);
        U = new Matrix(n, n);

        P = new Vector(n);

        for (int i = 0; i < n; i++) {
            P.set(i, i);
        }

        decompose(a);
    }

    private void decompose(Matrix A) {
        int n = A.getRows();

        for (int k = 0; k < n; k++) {
            int maxRow = k;

            for (int i = k + 1; i < n; i++) {
                if (Math.abs(A.get(i, k)) > Math.abs(A.get(maxRow, k))) {
                    maxRow = i;
                }
            }

            // Swap rows
            P.swap(k, maxRow);

            A.swapRows(k, maxRow);

            //check for singular matrix
            if (Math.abs(A.get(k, k)) < 1e-10) {
                singular = true;
                return;
            }

            // Compute L and U
            for (int i = k + 1; i < n; i++) {
                A.set(i, k, A.get(i, k) / A.get(k, k));

                for (int j = k + 1; j < n; j++) {
                    A.set(i, j, A.get(i, j) - (A.get(i, k) * A.get(k, j)));
                }
            }
        }

        // extract L and U from modified A
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i > j) {
                    L.set(i, j, A.get(i, j));
                } else if (i == j) {
                    L.set(i, j, 1);
                    U.set(i, j, A.get(i, j));
                } else {
                    U.set(i, j, A.get(i, j));
                }
            }
        }
    }

    /**
     * Gets the lower triangular matrix.
     * @return The lower triangular matrix.
     * */
    public Matrix getL() {
        return L;
    }

    /**
     * Gets the upper triangular matrix.
     * @return The upper triangular matrix.
     * */
    public Matrix getU() {
        return U;
    }

    /**
     * Gets the permutation vector.
     * @return The permutation vector.
     * */
    public Vector getP() {
        return P;
    }

    /**
     * Inverts the matrix.
     * @return The inverted matrix.
     * */
    public Matrix invert() {
        if (singular) {
            throw new IllegalStateException("Matrix is singular");
        }

        int n = L.getRows();
        double[][] inv = new double[n][n];
        double[][] identity = new double[n][n];

        for (int i = 0; i < n; i++) {
            identity[i][i] = 1;
        }

        // Solve for each column of the identity matrix
        for (int i = 0; i < n; i++) {
            double[] column = solve(identity[i]);
            for (int j = 0; j < n; j++) {
                inv[j][i] = column[j];
            }
        }

        return new Matrix(inv);
    }

    /**
     * Solves the system of linear equations Ax = b
     * @param b The right-hand side of the equation.
     * @return The solution to the system of linear equations.
     * */
    public double[] solve(double[] b) {
        int n = L.getMatrix().length;
        double[] y = new double[n];
        double[] x = new double[n];

        // Forward substitution to solve L * y = b
        for (int i = 0; i < n; i++) {
            y[i] = b[(int) P.getValues()[i]];
            for (int j = 0; j < i; j++) {
                y[i] -= L.getMatrix()[i][j] * y[j];
            }
        }

        // Backward substitution to solve U * x = y
        for (int i = n - 1; i >= 0; i--) {
            x[i] = y[i];
            for (int j = i + 1; j < n; j++) {
                x[i] -= U.getMatrix()[i][j] * x[j];
            }
            x[i] /= U.getMatrix()[i][i];
        }

        return x;
    }
}
