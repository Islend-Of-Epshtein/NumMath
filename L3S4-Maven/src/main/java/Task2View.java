
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

public class Task2View extends JPanel {
    private Task2Model model;
    private PlotPanel plotPanel;
    private JComboBox<Task2Model.FunctionType> functionCombo;
    private JSlider pointCountSlider;
    private JLabel pointCountLabel;

    private static final Color COLOR_SOURCE = Color.BLACK;
    private static final Color COLOR_LAGRANGE = Color.RED;
    private static final Color COLOR_NEWTON = Color.BLUE;
    private static final Color COLOR_SLAE = new Color(0, 128, 0);
    private static final Color COLOR_TABLE_POINTS = Color.MAGENTA;

    public Task2View(Task2Model model) {
        this.model = model;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1100, 700));

        initControlPanel();
        initPlotPanel();
    }

    private void initControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(new TitledBorder("Параметры интерполяции"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Функция:"), gbc);
        gbc.gridx = 1;
        functionCombo = new JComboBox<>(Task2Model.FunctionType.values());
        functionCombo.addActionListener(e -> updateModel());
        controlPanel.add(functionCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Количество точек (3-100):"), gbc);
        gbc.gridx = 1;
        pointCountSlider = new JSlider(3, 100, 10);
        pointCountSlider.setMajorTickSpacing(20);
        pointCountSlider.setPaintTicks(true);
        pointCountSlider.setPaintLabels(true);
        pointCountSlider.addChangeListener(e -> {
            pointCountLabel.setText(String.valueOf(pointCountSlider.getValue()));
            updateModel();
        });
        controlPanel.add(pointCountSlider, gbc);

        gbc.gridx = 2;
        pointCountLabel = new JLabel("10");
        controlPanel.add(pointCountLabel, gbc);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void initPlotPanel() {
        plotPanel = new PlotPanel();
        add(plotPanel, BorderLayout.CENTER);
    }

    private void updateModel() {
        model.setFunction((Task2Model.FunctionType) functionCombo.getSelectedItem());
        model.setPointCount(pointCountSlider.getValue());
        plotPanel.repaint();
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

            // Находим мин/макс Y
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (Task2Model.Point2D p : model.getSourceFunctionPoints()) {
                minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
            }
            for (Task2Model.Point2D p : model.getLagrangePoints()) {
                minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
            }
            minY -= 2; maxY += 2;

            drawGrid(g2d, w, h, minX, maxX, minY, maxY);
            drawAxes(g2d, w, h, minX, maxX, minY, maxY);

            // Рисуем графики
            drawCurve(g2d, model.getSourceFunctionPoints(), COLOR_SOURCE, new BasicStroke(2), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getLagrangePoints(), COLOR_LAGRANGE, new BasicStroke(1.5f), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getNewtonPoints(), COLOR_NEWTON, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 4}, 0), w, h, minX, maxX, minY, maxY);
            drawCurve(g2d, model.getSlaePoints(), COLOR_SLAE, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 4}, 0), w, h, minX, maxX, minY, maxY);

            // Точки интерполяции
            drawPoints(g2d, model.getTablePoints(), COLOR_TABLE_POINTS, w, h, minX, maxX, minY, maxY);

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

        private void drawCurve(Graphics2D g2d, List<Task2Model.Point2D> points, Color color, Stroke stroke, int w, int h, double minX, double maxX, double minY, double maxY) {
            if (points == null || points.isEmpty()) return;
            g2d.setColor(color);
            g2d.setStroke(stroke);
            GeneralPath path = new GeneralPath();
            boolean first = true;
            for (Task2Model.Point2D p : points) {
                int x = mapX(p.x, w, minX, maxX);
                int y = mapY(p.y, h, minY, maxY);
                if (first) { path.moveTo(x, y); first = false; }
                else { path.lineTo(x, y); }
            }
            g2d.draw(path);
        }

        private void drawPoints(Graphics2D g2d, List<Task2Model.Point2D> points, Color color, int w, int h, double minX, double maxX, double minY, double maxY) {
            g2d.setColor(color);
            for (Task2Model.Point2D p : points) {
                int x = mapX(p.x, w, minX, maxX);
                int y = mapY(p.y, h, minY, maxY);
                g2d.fillOval(x - 5, y - 5, 10, 10);
            }
        }

        private void drawLegend(Graphics2D g2d, int w, int h) {
            int x = w - 180;
            int y = margin + 20;
            int lineHeight = 20;

            String[] names = {"Исходная функция", "Лагранж", "Ньютон", "СЛАУ", "Точки интерполяции"};
            Color[] colors = {COLOR_SOURCE, COLOR_LAGRANGE, COLOR_NEWTON, COLOR_SLAE, COLOR_TABLE_POINTS};

            g2d.setFont(new Font("Dialog", Font.PLAIN, 11));
            for (int i = 0; i < names.length; i++) {
                int currentY = y + i * (lineHeight + 2);
                g2d.setColor(colors[i]);
                if (i == 4) {
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