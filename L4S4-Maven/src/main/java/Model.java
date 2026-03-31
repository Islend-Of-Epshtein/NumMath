// ==================== DerivativeModel.java ====================
import java.util.ArrayList;
import java.util.List;

public class Model {

    // Исходная функция f(x) = v * x^2, v = 9
    private static final double V = 9.0;

    // Точки для вычисления
    private final double[] points = {0.01, 0.1, 1.0, 10.0, 100.0};

    // Шаги для исследования
    private final double[] steps = {
            1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8
    };

    // Результаты вычислений
    private List<Result> results;

    // Внутренний класс для хранения результатов
    public static class Result {
        public double x;
        public double exactDerivative;
        public List<StepResult> stepResults;

        public Result(double x, double exactDerivative) {
            this.x = x;
            this.exactDerivative = exactDerivative;
            this.stepResults = new ArrayList<>();
        }
    }

    public static class StepResult {
        public double h;
        public double approxDerivative;
        public double absoluteError;
        public double relativeError;

        public StepResult(double h, double approx, double exact) {
            this.h = h;
            this.approxDerivative = approx;
            this.absoluteError = Math.abs(exact - approx);
            this.relativeError = this.absoluteError / Math.abs(exact);
        }
    }

    public Model() {
        calculateAll();
    }

    /**
     * Вычисление значения функции f(x) = 9 * x^2
     */
    public double f(double x) {
        return V * x * x;
    }

    /**
     * Точная производная f'(x) = 18 * x
     */
    public double exactDerivative(double x) {
        return 2 * V * x;
    }

    /**
     * Приближенная производная по формуле (4.3): (f(x+h) - f(x-h)) / (2h)
     */
    public double approximateDerivative(double x, double h) {
        return (f(x + h) - f(x - h)) / (2 * h);
    }

    /**
     * Выполнить все вычисления
     */
    private void calculateAll() {
        results = new ArrayList<>();

        for (double x : points) {
            Result result = new Result(x, exactDerivative(x));

            for (double h : steps) {
                double approx = approximateDerivative(x, h);
                result.stepResults.add(new StepResult(h, approx, result.exactDerivative));
            }

            results.add(result);
        }
    }

    /**
     * Поиск оптимального шага для заданной точки
     */
    public StepResult findOptimalStep(double x) {
        Result result = getResultForX(x);
        if (result == null) return null;

        StepResult optimal = null;
        for (StepResult sr : result.stepResults) {
            if (optimal == null || sr.absoluteError < optimal.absoluteError) {
                optimal = sr;
            }
        }
        return optimal;
    }

    /**
     * Поиск результата для заданной точки
     */
    public Result getResultForX(double x) {
        for (Result r : results) {
            if (Math.abs(r.x - x) < 1e-10) {
                return r;
            }
        }
        return null;
    }

    public double[] getPoints() {
        return points;
    }

    public double[] getSteps() {
        return steps;
    }

    public List<Result> getResults() {
        return results;
    }

    /**
     * Экспериментальное определение минимальной достижимой погрешности
     */
    public double findMinimalError(double x) {
        Result result = getResultForX(x);
        if (result == null) return Double.NaN;

        double minError = Double.MAX_VALUE;
        for (StepResult sr : result.stepResults) {
            if (sr.absoluteError < minError) {
                minError = sr.absoluteError;
            }
        }
        return minError;
    }
}