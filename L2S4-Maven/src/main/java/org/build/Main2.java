package org.build;

import java.util.Scanner;

public class Main2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите модуль для запуска:");
        System.out.println("1 - App (лаба 1, методы Гаусса)");
        System.out.println("2 - App2 (лаба 2, итерационные методы)");
        System.out.print("Ваш выбор: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.println("Запуск App...");
                new App(args);
                break;

            case "2":
                System.out.println("Запуск App2...");
                new App2(args);
                break;

            default:
                System.out.println("Неизвестный пункт. Введите 1 или 2.");
                break;
        }

        scanner.close();
    }
}