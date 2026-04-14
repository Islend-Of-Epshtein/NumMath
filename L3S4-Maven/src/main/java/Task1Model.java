
import java.util.ArrayList;
import java.util.List;

public class Task1Model {
    private List<Point2D> points;
    private double[] customPolyCoeffs;
    private double[] coeffs1, coeffs2, coeffs3;
    private List<Point2D> sourcePoints, lagrangePoints, newtonPoints;
    private List<Point2D> lsq1Points, lsq2Points, lsq3Points, customPolyPoints;
    private double minX, maxX, minY, maxY;
    private static final double STEP = 0.01;

    public static class Point2D {
        public double x, y;
        public Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    public Task1Model() {
        // Вариант 9 из таблицы
        points = new ArrayList<>();
        points.add(new Point2D(7, 8));
        points.add(new Point2D(9, 11));
        points.add(new Point2D(10, 7));
        points.add(new Point2D(12, 2));
        points.add(new Point2D(14, -5));

        customPolyCoeffs = new double[]{1, 2, -3, 0.5, -0.1};
        recalculateAll();
    }

    public void recalculateAll() {
        sourcePoints = new ArrayList<>();
        for (Point2D p : points) {
            sourcePoints.add(new Point2D(p.x, p.y));
        }

        calculateBounds();

        coeffs1 = leastSquares(1);
        coeffs2 = leastSquares(2);
        coeffs3 = leastSquares(3);

        lagrangePoints = new ArrayList<>();
        newtonPoints = new ArrayList<>();
        lsq1Points = new ArrayList<>();
        lsq2Points = new ArrayList<>();
        lsq3Points = new ArrayList<>();
        customPolyPoints = new ArrayList<>();

        for (double x = minX; x <= maxX; x += STEP) {
            lagrangePoints.add(new Point2D(x, lagrangePolynomial(x)));
            newtonPoints.add(new Point2D(x, newtonPolynomial(x)));
            lsq1Points.add(new Point2D(x, polynomialValue(coeffs1, x)));
            lsq2Points.add(new Point2D(x, polynomialValue(coeffs2, x)));
            lsq3Points.add(new Point2D(x, polynomialValue(coeffs3, x)));
            customPolyPoints.add(new Point2D(x, customPolynomial(x)));
        }
    }

    private void calculateBounds() {
        minX = Double.MAX_VALUE; maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE; maxY = -Double.MAX_VALUE;
        for (Point2D p : points) {
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
        }
        double xPadding = (maxX - minX) * 0.3;
        double yPadding = (maxY - minY) * 0.5;
        minX -= xPadding; maxX += xPadding;
        minY -= yPadding; maxY += yPadding;
        minY -= 10; maxY += 10;
    }

    private double lagrangePolynomial(double x) {
        int n = points.size();
        double result = 0;
        for (int i = 0; i < n; i++) {
            double term = points.get(i).y;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    term *= (x - points.get(j).x) / (points.get(i).x - points.get(j).x);
                }
            }
            result += term;
        }
        return result;
    }

    private double[][] computeDividedDifferences() {
        int n = points.size();
        double[][] diff = new double[n][n];
        for (int i = 0; i < n; i++) diff[i][0] = points.get(i).y;
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                diff[i][j] = (diff[i + 1][j - 1] - diff[i][j - 1]) / (points.get(i + j).x - points.get(i).x);
            }
        }
        return diff;
    }

    private double newtonPolynomial(double x) {
        double[][] diff = computeDividedDifferences();
        double result = diff[0][0];
        for (int j = 1; j < points.size(); j++) {
            double term = diff[0][j];
            for (int i = 0; i < j; i++) term *= (x - points.get(i).x);
            result += term;
        }
        return result;
    }

    private double[] leastSquares(int degree) {
        int n = points.size();
        int m = degree + 1;
        double[][] A = new double[m][m];
        double[] B = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += Math.pow(points.get(k).x, i + j);
                }
                A[i][j] = sum;
            }
            double sumB = 0;
            for (int k = 0; k < n; k++) {
                sumB += points.get(k).y * Math.pow(points.get(k).x, i);
            }
            B[i] = sumB;
        }
        return solveGauss(A, B, m);
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
        for (int i = 0; i < coeffs.length; i++) result += coeffs[i] * Math.pow(x, i);
        return result;
    }

    private double customPolynomial(double x) {
        double result = 0;
        for (int i = 0; i < customPolyCoeffs.length; i++) result += customPolyCoeffs[i] * Math.pow(x, i);
        return result;
    }

    // Геттеры
    public List<Point2D> getSourcePoints() { return sourcePoints; }
    public List<Point2D> getLagrangePoints() { return lagrangePoints; }
    public List<Point2D> getNewtonPoints() { return newtonPoints; }
    public List<Point2D> getLsq1Points() { return lsq1Points; }
    public List<Point2D> getLsq2Points() { return lsq2Points; }
    public List<Point2D> getLsq3Points() { return lsq3Points; }
    public List<Point2D> getCustomPolyPoints() { return customPolyPoints; }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
}