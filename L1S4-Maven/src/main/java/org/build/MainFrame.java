package org.build;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

//JOptionPane.showMessageDialog(this, "Введите число!");
public class MainFrame  extends JFrame implements PropertyChangeListener
{
    private Main model;
    private JWindow window;
    private JTextArea ta1 ,ta2, ta3;
    private JLabel label;
    private JTextArea iterTextArea;
    private final Font font = new Font("Arial",Font.BOLD, 25);
    public MainFrame(Main p) {
        this.model=p;
        setSize(500, 400);
        setLocationRelativeTo(null);
        setTitle("Вычисление нулей функции");
        ImageIcon img = new ImageIcon("L1S4-Maven/Icon.png");
        setIconImage(img.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }
    private void initComponents()
    {
        JPanel mainpanel = new JPanel(new BorderLayout());
        JMenuBar menubar = new JMenuBar();
        JMenu graf = new JMenu("График");
        menubar.add(graf);
        JMenuItem Show = new JMenuItem("Построить график.");
        Show.addActionListener(e-> new Grafic(this.getLocation().x, this.getLocation().y));
        graf.add(Show);
        JMenu NumMenu = new JMenu("Переменные");
        menubar.add(NumMenu);
        JMenu СheckMenu = new JMenu("Отладка");
        menubar.add(СheckMenu);
        JMenuItem showIteration = new JMenuItem("Показывать историю итераций");
        JMenuItem closeIteration = new JMenuItem("Закрыть окно итераций");
        СheckMenu.add(showIteration);
        СheckMenu.add(closeIteration);
        closeIteration.addActionListener(e->{
            CloseIter();
        });
        showIteration.addActionListener(e->{
            ShowIter();
        });

        JMenuItem editEps = new JMenuItem("Задать постоянную epsilon");
        JMenuItem editCount = new JMenuItem("Максимальное количество итераций");
        NumMenu.add(editEps);
        NumMenu.add(editCount);
        editEps.addActionListener(e->
        {
            ChangeNumeral("Значение точности (eps):", "eps");
        });
        editCount.addActionListener(e->
        {
            ChangeNumeral("Максимальное количесвто итераций(Count):", "count");
        });


        setJMenuBar(menubar);

        JPanel gridpanel = new JPanel(new GridLayout(0,2));
        ArrayList<JButton> btns = new ArrayList<>(Arrays.asList(
                createStyledButton("<html><center>Половинный<br>интервал</center></html>"),
                createStyledButton("<html><center>Метод<br>секущих</center></html>"),
                createStyledButton("<html><center>Метод<br>касательных</center></html>"),
                createStyledButton("<html><center>Метод<br>хорд</center></html>"),
                createStyledButton("<html><center>Метод<br>Простых итер-ций</center></html>")
        ));
        ArrayList<JLabel> lbs = new ArrayList<>(Arrays.asList(
                createStyledLabel("Вывод 1"),
                createStyledLabel("Вывод 2"),
                createStyledLabel("Вывод 3"),
                createStyledLabel("Вывод 4"),
                createStyledLabel("Вывод 5")
        ));
        for(int x=0,y=0;x!=5;x++,y++){
            gridpanel.add(btns.get(x));
            gridpanel.add(lbs.get(y));
        }
        addMethodListener(btns.get(0),"half" ,lbs.get(0),() -> model.HalfDivisionMethod(getX1(),getX2(), getX0()));
        addMethodListener(btns.get(1),"secant" ,lbs.get(1),() -> model.SecantMethod(getX1(),getX2(), getX0()));
        addMethodListener(btns.get(2),"newton" ,lbs.get(2),() -> model.TangentMethod(getX1(),getX2(), getX0()));
        addMethodListener(btns.get(3),"chord" ,lbs.get(3),() -> model.ChordMethod(getX1(),getX2(), getX0()));
        addMethodListener(btns.get(4),"iteration" ,lbs.get(4),() -> model.MethodOfSimpleIterations(getX1(),getX2(), getX0()));

        JPanel labelpanel = new JPanel(new BorderLayout());

        JPanel textbar = new JPanel(new FlowLayout());
        textbar.setBackground(Color.darkGray);
        textbar.setFocusable(true);
        JLabel lb6 = createStyledLabel("x1:");
        JLabel lb7 = createStyledLabel("x2:");
        JLabel lb8 = createStyledLabel("(option) x0:");

        ta1 = createStyledTextArea("3");
        ta2 = createStyledTextArea("4");
        ta3 = createStyledTextArea("NaN");
        textbar.add(lb6);textbar.add(ta1);
        textbar.add(lb7);textbar.add(ta2);
        textbar.add(lb8);textbar.add(ta3);

        labelpanel.add(textbar);

        mainpanel.add(labelpanel, BorderLayout.SOUTH);
        mainpanel.add(gridpanel, BorderLayout.CENTER);

        add(mainpanel);
    }
    private void addMethodListener(JButton button, String methodName,
                                   JLabel resultLabel, Runnable methodCall) {
        button.addActionListener(e -> {
            try {
                methodCall.run();
                resultLabel.setText("Res: " + getMethodResult(methodName));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
    private double getMethodResult(String methodName) {
        switch (methodName) {
            case "half": return model.getHDMR();
            case "secant": return model.getSM();
            case "newton": return model.getTM();
            case "chord": return model.getCM();
            case "iteration": return model.getMOSI();
            default: return 0;
        }
    }
    private Double getX0()
    {
        if(!TryParseDouble(ta3.getText())){
            return Double.NaN;
        }
        return Double.parseDouble(ta3.getText());
    }
    private double getX1()
    {
        if(!TryParseDouble(ta1.getText()))
        {
            throw new NumberFormatException("X1 - введите правильное число");
        }
        return Double.parseDouble(ta1.getText());
    }
    private double getX2()
    {
        if(!TryParseDouble(ta2.getText()))
        {
            throw new NumberFormatException("X2 - введите правильное число");
        }
        return Double.parseDouble(ta2.getText());
    }
    private boolean TryParseDouble(String str){
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException ex){
            return false;
        }
    }
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.darkGray);
        btn.setFont(new Font("Arial",Font.BOLD,20));
        btn.setForeground(Color.CYAN);
        btn.setFocusPainted(false);
        return btn;
    }
    private JLabel createStyledLabel(String text) {
        JLabel lb1 = new JLabel();
        lb1.setOpaque(true);
        lb1.setVerticalTextPosition(JLabel.CENTER);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        lb1.setText(text);
        lb1.setFont(font);
        lb1.setBackground(Color.DARK_GRAY);
        lb1.setForeground(Color.CYAN);
        return lb1;
    }
    private JTextArea createStyledTextArea(String text) {
        JTextArea ta = new JTextArea(1,2);
        ta.setOpaque(true);
        ta.setText(text);
        ta.setFont(font);
        ta.setBackground(Color.GRAY);
        ta.setForeground(Color.CYAN);
        return ta;
    }
    private void ChangeNumeral(String str, String name) {
        String input = JOptionPane.showInputDialog(
                this,
                str,
                Objects.equals(name, "eps") ?model.getEps():model.getCount()
        );
        if (input != null) {
            switch (name){
                case "eps":
                {
                    try {
                        double eps = Double.parseDouble(input);
                        if (eps > 0) {
                            model.setEps(eps);
                            JOptionPane.showMessageDialog(this,
                                    "установлено: "+name+" = " + eps);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Значение должно быть положительным!",
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Некорректный формат числа!",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }
                case"count":
                {
                    try {
                        int eps = Integer.parseInt(input);
                        if (eps > 0) {
                            model.setCount(eps);
                            JOptionPane.showMessageDialog(this,
                                    "установлено: "+name+" = " + eps);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Значение должно быть положительным!",
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Некорректный формат числа!",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        }
    }
    private void ShowIter()
    {
        this.window = new JWindow();
        window.setLocation(this.getLocation().x - 150, this.getLocation().y + 50);
        window.setSize(150, 300);
        model.addListener(this);

        JPanel panel = new JPanel(new BorderLayout());

        // Заголовок
        label = new JLabel("Стек итераций:");
        label.setBackground(Color.GRAY);
        label.setForeground(Color.CYAN);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true); // чтобы фон отображался
        panel.add(label, BorderLayout.NORTH);

        // Создаём текстовую область для отображения итераций
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // Сохраняем ссылку на textArea для обновления (добавьте поле в класс)
        this.iterTextArea = textArea;

        // Помещаем текстовую область в JScrollPane для прокрутки
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        panel.add(scrollPane, BorderLayout.CENTER);

        window.add(panel);
        window.setVisible(true);

        // Первоначальное обновление
        updateLabel();
    }
    private void CloseIter(){
        if(window!=null){ this.window.dispose();}
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (window!=null && Objects.equals(evt.getPropertyName(), "approximations")){
            updateLabel();
        }
    }
    public void updateLabel(){
        if (iterTextArea == null) return; // проверка на случай, если окно закрыто

        List<Double> list = model.getApprox();
        if (list == null || list.isEmpty()) {
            iterTextArea.setText("Список пуст");
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Добавляем заголовок для красоты
        sb.append("─── Итерации ───\n");
        sb.append(" №     Значение\n");
        sb.append("────────────────\n");

        for (int i = 0; i < list.size(); i++) {
            sb.append(String.format("%2d → %.6f\n", i, list.get(i)));
        }

        sb.append("────────────────\n");
        sb.append(String.format("Всего: %d итераций", list.size()));

        iterTextArea.setText(sb.toString());

        // Автопрокрутка вниз (к последним значениям)
        iterTextArea.setCaretPosition(iterTextArea.getDocument().getLength());
    }
}