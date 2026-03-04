package org.build;

import javax.swing.*;
import java.awt.*;

public class MainFrame2 extends MainFrame {

    public MainFrame2(Main p) {
        super(p);
        super.initComponents();
        AddSwitchMenu();
        setVisible(true);
    }

    protected void initComponents2()
    {
        menuBar.removeAll();
        AddSwitchMenu();
        Container p = new Container();
        Panel panel = new Panel(new FlowLayout());
        p.add(panel);
        add(p);
    }
    protected void AddSwitchMenu(){
        JMenu option = new JMenu("Лабораторные");
        JMenuItem Lab1 = new JMenuItem("лабораторная 1");
        JMenuItem Lab2 = new JMenuItem("лабораторная 2");
        option.add(Lab1);
        option.add(Lab2);
        menuBar.add(option);
        Lab1.addActionListener(e -> {
            getContentPane().removeAll();
            super.initComponents();
            AddSwitchMenu();
            setVisible(true);
        });
        Lab2.addActionListener(e -> {
            getContentPane().removeAll();
            this.initComponents2();
            setVisible(true);
        });
    }
    // Переопределяем метод для изменения поведения
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        super.propertyChange(evt); // вызываем родительский метод

        // Добавляем свою логику
        if (evt.getPropertyName().equals("approximations")) {
        }
    }
}