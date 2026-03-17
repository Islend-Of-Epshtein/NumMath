package org.build;

public final class GaussianEliminationSolver {

    private static final float EPS = 1e-7f;

    private GaussianEliminationSolver() {
    }

    public static final class Result {
        public final float[] solution;
        public final float[][] upperMatrix;
        public final float[] transformedRightSide;
        public final int[] columnOrder;
        public final GaussianPivotMode pivotMode;

        public Result(float[] solution,
                      float[][] upperMatrix,
                      float[] transformedRightSide,
                      int[] columnOrder,
                      GaussianPivotMode pivotMode) {
            this.solution = solution;
            this.upperMatrix = upperMatrix;
            this.transformedRightSide = transformedRightSide;
            this.columnOrder = columnOrder;
            this.pivotMode = pivotMode;
        }
    }

    public static Result solve(float[][] a, float[] b, GaussianPivotMode pivotMode) {
        MatrixUtils.validateLinearSystem(a, b);

        float[][] matrix = MatrixUtils.copy(a);
        float[] rhs = MatrixUtils.copy(b);
        int n = matrix.length;
        int[] columnOrder = new int[n];

        for (int i = 0; i < n; i++) {
            columnOrder[i] = i;
        }

        for (int k = 0; k < n - 1; k++) {
            Pivot pivot = choosePivot(matrix, k, pivotMode);

            if (pivot == null || Math.abs(matrix[pivot.row][pivot.col]) < EPS) {
                throw new IllegalArgumentException(
                        "Система вырождена или требует другого выбора главного элемента на шаге " + k
                );
            }

            if (pivot.row != k) {
                swapRows(matrix, rhs, pivot.row, k);
            }
            if (pivot.col != k) {
                swapColumns(matrix, columnOrder, pivot.col, k);
            }

            float diagonal = matrix[k][k];
            if (Math.abs(diagonal) < EPS) {
                throw new IllegalArgumentException(
                        "Нулевой диагональный элемент после перестановки на шаге " + k
                );
            }

            for (int i = k + 1; i < n; i++) {
                float factor = matrix[i][k] / diagonal;
                matrix[i][k] = 0f;

                for (int j = k + 1; j < n; j++) {
                    matrix[i][j] -= factor * matrix[k][j];
                }

                rhs[i] -= factor * rhs[k];
            }
        }

        float[] reordered = backwardSubstitution(matrix, rhs);
        float[] solution = restoreColumnOrder(reordered, columnOrder);

        return new Result(solution, matrix, rhs, columnOrder, pivotMode);
    }

    private static float[] backwardSubstitution(float[][] matrix, float[] rhs) {
        int n = rhs.length;
        float[] x = new float[n];

        for (int i = n - 1; i >= 0; i--) {
            float sum = rhs[i];

            for (int j = i + 1; j < n; j++) {
                sum -= matrix[i][j] * x[j];
            }

            float diagonal = matrix[i][i];
            if (Math.abs(diagonal) < EPS) {
                throw new IllegalArgumentException(
                        "Нулевой диагональный элемент при обратном ходе на строке " + i
                );
            }

            x[i] = sum / diagonal;
        }

        return x;
    }

    private static float[] restoreColumnOrder(float[] reordered, int[] columnOrder) {
        float[] solution = new float[reordered.length];
        for (int i = 0; i < reordered.length; i++) {
            solution[columnOrder[i]] = reordered[i];
        }
        return solution;
    }

    private static Pivot choosePivot(float[][] matrix, int k, GaussianPivotMode mode) {
        switch (mode) {
            case NONE:
                return new Pivot(k, k);
            case PARTIAL_BY_COLUMN:
                return choosePartialPivot(matrix, k);
            case FULL:
                return chooseFullPivot(matrix, k);
            default:
                throw new IllegalArgumentException("Unsupported pivot mode: " + mode);
        }
    }

    private static Pivot choosePartialPivot(float[][] matrix, int k) {
        int bestRow = k;
        float bestValue = Math.abs(matrix[k][k]);

        for (int i = k + 1; i < matrix.length; i++) {
            float value = Math.abs(matrix[i][k]);
            if (value > bestValue) {
                bestValue = value;
                bestRow = i;
            }
        }

        return new Pivot(bestRow, k);
    }

    private static Pivot chooseFullPivot(float[][] matrix, int k) {
        int bestRow = k;
        int bestCol = k;
        float bestValue = Math.abs(matrix[k][k]);

        for (int i = k; i < matrix.length; i++) {
            for (int j = k; j < matrix.length; j++) {
                float value = Math.abs(matrix[i][j]);
                if (value > bestValue) {
                    bestValue = value;
                    bestRow = i;
                    bestCol = j;
                }
            }
        }

        return new Pivot(bestRow, bestCol);
    }

    private static void swapRows(float[][] matrix, float[] rhs, int first, int second) {
        float[] tempRow = matrix[first];
        matrix[first] = matrix[second];
        matrix[second] = tempRow;

        float tempValue = rhs[first];
        rhs[first] = rhs[second];
        rhs[second] = tempValue;
    }

    private static void swapColumns(float[][] matrix, int[] columnOrder, int first, int second) {
        for (int i = 0; i < matrix.length; i++) {
            float temp = matrix[i][first];
            matrix[i][first] = matrix[i][second];
            matrix[i][second] = temp;
        }

        int tempIndex = columnOrder[first];
        columnOrder[first] = columnOrder[second];
        columnOrder[second] = tempIndex;
    }

    private static final class Pivot {
        private final int row;
        private final int col;

        private Pivot(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}