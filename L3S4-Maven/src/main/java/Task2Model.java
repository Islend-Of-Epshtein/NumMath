
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Task2Model {
    public enum FunctionType {
        SIN("sin(x)"),
        SQUARE("x²"),
        EXP("eˣ/10");

        private final String formula;
        FunctionType(String formula) { this.formula = formula; }
        public String getFormula() { return formula; }
    }

    private FunctionType currentFunction = FunctionType.SIN;
    private int pointCount = 10;
    private double minX = 0.0;
    private double maxX = 10.0;
    private static final double STEP = 0.01;

    private List<Point2D> tablePoints;
    private List<Point2D> sourceFunctionPoints;
    private List<Point2D> lagrangePoints;
    private List<Point2D> newtonPoints;
    private List<Point2D> slaePoints;
    private double[] polyCoeffs;

    public static class Point2D {
        public double x, y;
        public Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    public Task2Model() {
        tablePoints = new ArrayList<>();
        generateTablePoints();
        recalculateAll();
    }

    public void generateTablePoints() {
        tablePoints.clear();
        if (pointCount <= 1) return;
        double h = (maxX - minX) / (pointCount - 1);
        for (int i = 0; i < pointCount; i++) {
            double x = minX + i * h;
            tablePoints.add(new Point2D(x, evaluateFunction(x)));
        }
    }

    public double evaluateFunction(double x) {
        switch (currentFunction) {
            case SIN: return Math.sin(x);
            case SQUARE: return x * x;
            case EXP: return Math.exp(x) / 10.0;
            default: return 0;
        }
    }

    public void recalculateAll() {
        generateTablePoints();

        // Исходная функция
        sourceFunctionPoints = new ArrayList<>();
        for (double x = minX; x <= maxX; x += STEP) {
            sourceFunctionPoints.add(new Point2D(x, evaluateFunction(x)));
        }

        // Лагранж
        lagrangePoints = new ArrayList<>();
        for (double x = minX; x <= maxX; x += STEP) {
            lagrangePoints.add(new Point2D(x, lagrangePolynomial(x)));
        }

        // Ньютон
        newtonPoints = new ArrayList<>();
        double[][] divDiff = computeDividedDifferences();
        for (double x = minX; x <= maxX; x += STEP) {
            newtonPoints.add(new Point2D(x, newtonPolynomial(x, divDiff)));
        }

        // СЛАУ
        solveSlaeForPoly();
        slaePoints = new ArrayList<>();
        for (double x = minX; x <= maxX; x += STEP) {
            slaePoints.add(new Point2D(x, polynomialValue(polyCoeffs, x)));
        }
    }

    private double lagrangePolynomial(double x) {
        int n = tablePoints.size();
        double result = 0;
        for (int i = 0; i < n; i++) {
            double term = tablePoints.get(i).y;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    term *= (x - tablePoints.get(j).x) / (tablePoints.get(i).x - tablePoints.get(j).x);
                }
            }
            result += term;
        }
        return result;
    }

    private double[][] computeDividedDifferences() {
        int n = tablePoints.size();
        double[][] diff = new double[n][n];
        for (int i = 0; i < n; i++) diff[i][0] = tablePoints.get(i).y;
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                diff[i][j] = (diff[i + 1][j - 1] - diff[i][j - 1]) / (tablePoints.get(i + j).x - tablePoints.get(i).x);
            }
        }
        return diff;
    }

    private double newtonPolynomial(double x, double[][] diff) {
        int n = tablePoints.size();
        double result = diff[0][0];
        for (int j = 1; j < n; j++) {
            double term = diff[0][j];
            for (int i = 0; i < j; i++) term *= (x - tablePoints.get(i).x);
            result += term;
        }
        return result;
    }

    private void solveSlaeForPoly() {
        int n = tablePoints.size();
        double[][] A = new double[n][n];
        double[] B = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = Math.pow(tablePoints.get(i).x, j);
            }
            B[i] = tablePoints.get(i).y;
        }
        polyCoeffs = solveGauss(A, B, n);
    }

    private double[] solveGauss(double[][] a, double[] b, int n) {
        double[][] A = new double[n][n];
        double[] B = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, A[i], 0, n);
            B[i] = b[i];
        }
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) maxRow = k;
            }
            double[] tempRow = A[i]; A[i] = A[maxRow]; A[maxRow] = tempRow;
            double tempB = B[i]; B[i] = B[maxRow]; B[maxRow] = tempB;
            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                B[k] -= factor * B[i];
                for (int j = i; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
            }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = B[i];
            for (int j = i + 1; j < n; j++) x[i] -= A[i][j] * x[j];
            x[i] /= A[i][i];
        }
        return x;
    }

    private double polynomialValue(double[] coeffs, double x) {
        double result = 0;
        for (int i = 0; i < coeffs.length; i++) {
            result += coeffs[i] * Math.pow(x, i);
        }
        return result;
    }

    // Геттеры и сеттеры
    public List<Point2D> getSourceFunctionPoints() { return sourceFunctionPoints; }
    public List<Point2D> getLagrangePoints() { return lagrangePoints; }
    public List<Point2D> getNewtonPoints() { return newtonPoints; }
    public List<Point2D> getSlaePoints() { return slaePoints; }
    public List<Point2D> getTablePoints() { return tablePoints; }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }

    public void setFunction(FunctionType type) { this.currentFunction = type; recalculateAll(); }
    public void setPointCount(int count) { this.pointCount = Math.max(3, Math.min(100, count)); recalculateAll(); }
}