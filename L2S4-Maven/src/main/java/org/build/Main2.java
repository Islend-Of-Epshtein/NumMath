package org.build;

import java.util.Scanner;

public class Main2{

    public static void main(String[] args) {
        new MainFrame2(new App1(args));
        /*
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.println("Запуск App...");
                new App1(args);
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

         */
    }
}