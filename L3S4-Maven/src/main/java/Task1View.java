
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

public class Task1View extends JPanel {
    private Task1Model model;
    private PlotPanel plotPanel;
    private JLabel infoLabel;

    private static final Color COLOR_SOURCE = Color.BLACK;
    private static final Color COLOR_LAGRANGE = Color.RED;
    private static final Color COLOR_NEWTON = Color.BLUE;
    private static final Color COLOR_LSQ1 = new Color(0, 128, 0);
    private static final Color COLOR_LSQ2 = new Color(255, 165, 0);
    private static final Color COLOR_LSQ3 = new Color(128, 0, 128);
    private static final Color COLOR_CUSTOM = new Color(165, 42, 42);

    public Task1View(Task1Model model) {
        this.model = model;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1100, 700));

        initControlPanel();
        initPlotPanel();
    }

    private void initControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new TitledBorder("Информация"));

        infoLabel = new JLabel("Исходные точки: (7,8), (9,11), (10,7), (12,2), (14,-5)");
        controlPanel.add(infoLabel);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void initPlotPanel() {
        plotPanel = new PlotPanel();
        add(plotPanel, BorderLayout.CENTER);
    }

    private class PlotPanel extends JPanel {
        private int margin = 60;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            double minX = model.getMinX();
            double maxX = model.getMaxX();
            double minY = model.getMinY();
            double maxY = model.getMaxY();

            drawGrid(g2d, w, h, minX, maxX, minY, maxY);
            drawAxes(g2d, w, h, minX, maxX, minY, maxY);

            // Рисуем все графики
            drawCurve(g2d, model.getLsq1Points(), COLOR_LSQ1, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4}, 0), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getLsq2Points(), COLOR_LSQ2, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8, 4, 2, 4, 2, 4}, 0), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getLsq3Points(), COLOR_LSQ3, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getCustomPolyPoints(), COLOR_CUSTOM, new BasicStroke(3), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getLagrangePoints(), COLOR_LAGRANGE, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 10}, 0), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getNewtonPoints(), COLOR_NEWTON, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0), w, h, minX, maxX, minY, maxY);

            // Исходные точки
            drawPoints(g2d, model.getSourcePoints(), COLOR_SOURCE, w, h, minX, maxX, minY, maxY);

            drawLegend(g2d, w, h);
        }

        private void drawGrid(Graphics2D g2d, int w, int h, double minX, double maxX, double minY, double maxY) {
            g2d.setColor(new Color(220, 220, 220));
            for (int i = 0; i <= 10; i++) {
                double x = minX + (maxX - minX) * i / 10.0;
                int sx = mapX(x, w, minX, maxX);
                g2d.drawLine(sx, margin, sx, h - margin);

                double y = minY + (maxY - minY) * i / 10.0;
                int sy = mapY(y, h, minY, maxY);
                g2d.drawLine(margin, sy, w - margin, sy);
            }
        }

        private void drawAxes(Graphics2D g2d, int w, int h, double minX, double maxX, double minY, double maxY) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            int originX = mapX(0, w, minX, maxX);
            int originY = mapY(0, h, minY, maxY);

            if (originX >= margin && originX <= w - margin) {
                g2d.drawLine(originX, margin, originX, h - margin);
            }
            if (originY >= margin && originY <= h - margin) {
                g2d.drawLine(margin, originY, w - margin, originY);
            }

            g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
            g2d.drawString("X", w - margin + 5, originY);
            g2d.drawString("Y", originX, margin - 5);
        }

        private void drawCurve(Graphics2D g2d, List<Task1Model.Point2D> points, Color color, Stroke stroke, int w, int h, double minX, double maxX, double minY, double maxY) {
            if (points == null || points.isEmpty()) return;
            g2d.setColor(color);
            g2d.setStroke(stroke);
            GeneralPath path = new GeneralPath();
            boolean first = true;
            for (Task1Model.Point2D p : points) {
                int x = mapX(p.x, w, minX, maxX);
                int y = mapY(p.y, h, minY, maxY);
                if (first) { path.moveTo(x, y); first = false; }
                else { path.lineTo(x, y); }
            }
            g2d.draw(path);
        }

        private void drawPoints(Graphics2D g2d, List<Task1Model.Point2D> points, Color color, int w, int h, double minX, double maxX, double minY, double maxY) {
            g2d.setColor(color);
            for (Task1Model.Point2D p : points) {
                int x = mapX(p.x, w, minX, maxX);
                int y = mapY(p.y, h, minY, maxY);
                g2d.fillOval(x - 5, y - 5, 10, 10);
            }
        }

        private void drawLegend(Graphics2D g2d, int w, int h) {
            int x = w - 200;
            int y = margin + 20;
            int lineHeight = 20;

            String[] names = {"Исходные точки", "Лагранж", "Ньютон", "МНК степень 1", "МНК степень 2", "МНК степень 3", "Многочлен 4 степени"};
            Color[] colors = {COLOR_SOURCE, COLOR_LAGRANGE, COLOR_NEWTON, COLOR_LSQ1, COLOR_LSQ2, COLOR_LSQ3, COLOR_CUSTOM};

            g2d.setFont(new Font("Dialog", Font.PLAIN, 11));
            for (int i = 0; i < names.length; i++) {
                int currentY = y + i * (lineHeight + 2);
                g2d.setColor(colors[i]);
                if (i == 0) {
                    g2d.fillOval(x, currentY - 8, 12, 12);
                } else {
                    g2d.drawLine(x, currentY - 4, x + 20, currentY - 4);
                }
                g2d.setColor(Color.BLACK);
                g2d.drawString(names[i], x + 25, currentY);
            }
        }

        private int mapX(double x, int width, double minX, double maxX) {
            return margin + (int)((x - minX) / (maxX - minX) * (width - 2 * margin));
        }

        private int mapY(double y, int height, double minY, double maxY) {
            return height - margin - (int)((y - minY) / (maxY - minY) * (height - 2 * margin));
        }
    }
}