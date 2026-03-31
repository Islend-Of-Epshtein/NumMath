// ==================== SplineView.java ====================
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

public class SplineView extends JPanel {

    private SplineModel model;

    // Цвета
    private static final Color COLOR_SOURCE = Color.BLACK;
    private static final Color COLOR_SPLINE = new Color(0, 0, 255);      // Синий
    private static final Color COLOR_FIRST_DERIV = new Color(255, 0, 0); // Красный
    private static final Color COLOR_SECOND_DERIV = new Color(0, 128, 0);// Зеленый

    // Масштабирование
    private double minX, maxX, minY, maxY;
    private double minY1, maxY1;
    private double minY2, maxY2;

    public SplineView(SplineModel model) {
        this.model = model;
        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.WHITE);

        calculateBounds();
    }

    private void calculateBounds() {
        // Границы по X
        double[] x = model.getX();
        minX = x[0];
        maxX = x[x.length - 1];
        double xPadding = (maxX - minX) * 0.2;
        minX -= xPadding;
        maxX += xPadding;

        // Границы для сплайна
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;
        for (SplineModel.Point2D p : model.getSplinePoints()) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        double yPadding = (maxY - minY) * 0.2;
        minY -= yPadding;
        maxY += yPadding;

        // Границы для первой производной
        minY1 = Double.MAX_VALUE;
        maxY1 = -Double.MAX_VALUE;
        for (SplineModel.Point2D p : model.getFirstDerivativePoints()) {
            minY1 = Math.min(minY1, p.y);
            maxY1 = Math.max(maxY1, p.y);
        }
        double y1Padding = (maxY1 - minY1) * 0.2;
        minY1 -= y1Padding;
        maxY1 += y1Padding;

        // Границы для второй производной
        minY2 = Double.MAX_VALUE;
        maxY2 = -Double.MAX_VALUE;
        for (SplineModel.Point2D p : model.getSecondDerivativePoints()) {
            minY2 = Math.min(minY2, p.y);
            maxY2 = Math.max(maxY2, p.y);
        }
        double y2Padding = (maxY2 - minY2) * 0.2;
        minY2 -= y2Padding;
        maxY2 += y2Padding;
    }

    private int mapX(double x, int width, int padding) {
        return padding + (int) ((x - minX) / (maxX - minX) * (width - 2 * padding));
    }

    private int mapY(double y, int height, int padding, double min, double max) {
        return padding + (int) ((max - y) / (max - min) * (height - 2 * padding));
    }

    private void drawCurve(Graphics2D g2d, List<SplineModel.Point2D> points,
                           Color color, Stroke stroke, int width, int height,
                           int padding, double minY, double maxY) {
        if (points == null || points.isEmpty()) return;

        g2d.setColor(color);
        g2d.setStroke(stroke);

        GeneralPath path = new GeneralPath();
        boolean first = true;

        for (SplineModel.Point2D p : points) {
            int x = mapX(p.x, width, padding);
            int y = mapY(p.y, height, padding, minY, maxY);

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }

        g2d.draw(path);
    }

    private void drawPoints(Graphics2D g2d, double[] x, double[] y,
                            Color color, int width, int height, int padding,
                            double minY, double maxY) {
        g2d.setColor(color);
        for (int i = 0; i < x.length; i++) {
            int px = mapX(x[i], width, padding);
            int py = mapY(y[i], height, padding, minY, maxY);
            g2d.fillOval(px - 4, py - 4, 8, 8);
        }
    }

    private void drawGridAndAxes(Graphics2D g2d, int width, int height, int padding,
                                 double minY, double maxY, String yLabel) {
        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        // Сетка
        g2d.setColor(new Color(230, 230, 230));
        g2d.setStroke(new BasicStroke(1));

        for (int i = 0; i <= 10; i++) {
            int x = padding + i * graphWidth / 10;
            int y = padding + i * graphHeight / 10;
            g2d.drawLine(x, padding, x, height - padding);
            g2d.drawLine(padding, y, width - padding, y);
        }

        // Оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        int originX = mapX(0, width, padding);
        int originY = mapY(0, height, padding, minY, maxY);

        if (originX >= padding && originX <= width - padding) {
            g2d.drawLine(originX, padding, originX, height - padding);
        }
        if (originY >= padding && originY <= height - padding) {
            g2d.drawLine(padding, originY, width - padding, originY);
        }

        // Подписи осей
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString("X", width - padding - 15, originY - 5);

        Graphics2D g2dRot = (Graphics2D) g2d.create();
        g2dRot.translate(padding - 20, height / 2);
        g2dRot.rotate(-Math.PI / 2);
        g2dRot.drawString(yLabel, -30, 0);
        g2dRot.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int graphHeight = (height - 2 * padding) / 3;

        // ========== Верхний график: Сплайн ==========
        int yOffset1 = padding;
        drawGridAndAxes(g2d, width, graphHeight, padding, minY, maxY, "S(x)");
        drawCurve(g2d, model.getSplinePoints(), COLOR_SPLINE, new BasicStroke(2.5f),
                width, graphHeight, padding, minY, maxY);
        drawPoints(g2d, model.getX(), model.getY(), COLOR_SOURCE,
                width, graphHeight, padding, minY, maxY);

        // Подпись для сплайна
        g2d.setColor(COLOR_SPLINE);
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString("Кубический сплайн S(x)", width - 150, padding + 20);
        g2d.setColor(COLOR_SOURCE);
        g2d.drawString("Исходные точки", width - 150, padding + 40);

        // ========== Средний график: Первая производная ==========
        int yOffset2 = padding + graphHeight + 20;
        drawGridAndAxes(g2d, width, graphHeight, padding, minY1, maxY1, "S'(x)");
        drawCurve(g2d, model.getFirstDerivativePoints(), COLOR_FIRST_DERIV, new BasicStroke(2f),
                width, graphHeight, padding, minY1, maxY1);

        g2d.setColor(COLOR_FIRST_DERIV);
        g2d.drawString("Первая производная S'(x)", width - 150, yOffset2 + 20);

        // ========== Нижний график: Вторая производная ==========
        int yOffset3 = yOffset2 + graphHeight + 20;
        drawGridAndAxes(g2d, width, graphHeight, padding, minY2, maxY2, "S''(x)");
        drawCurve(g2d, model.getSecondDerivativePoints(), COLOR_SECOND_DERIV, new BasicStroke(2f),
                width, graphHeight, padding, minY2, maxY2);

        g2d.setColor(COLOR_SECOND_DERIV);
        g2d.drawString("Вторая производная S''(x)", width - 150, yOffset3 + 20);

        // Рамка
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(0, 0, width - 1, height - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SplineModel model = new SplineModel();
            SplineView view = new SplineView(model);

            JFrame frame = new JFrame("Кубический сплайн - Лабораторная работа 4 (Вариант 9)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}