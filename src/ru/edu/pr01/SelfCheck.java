package ru.edu.pr01;

public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        // Проверка методов расчёта
        boolean ok = false;
        try {
            ok = OrderCalculator.calculateBase(2, 100.0) == 200.0
                    && Math.abs(OrderCalculator.calculateTotal(2, 100.0, 10.0, 20.0) - 216.0) < 0.001;
        } catch (Exception e) {
            System.out.println("FAIL exception " + e.getMessage());
        }
        check("BASE 2 x 100, скидка 10%, НДС 20% = 216", ok);

        // Проверка значений из sample.csv
        System.out.println();
        System.out.println("=== sample.csv ===");
        check("CSV 1  12;2490.50;5 -> корректно", isValidLine("12;2490.50;5"));
        check("CSV 2  100;150.00;0 -> корректно", isValidLine("100;150.00;0"));
        check("CSV 3  0;1000;10 -> ошибка", !isValidLine("0;1000;10"));
        check("CSV 4  5;-20;5 -> ошибка", !isValidLine("5;-20;5"));
        check("CSV 5  3;999.99;35 -> ошибка", !isValidLine("3;999.99;35"));

        // Корректное типовое значение: полный расчёт с обработкой
        // 12 x 2490.50 = 29886.00; скидка 5% -> 28391.70; НДС 20% -> 34070.04; меньше 20 шт. -> +500
        System.out.println();
        System.out.println("=== Типовое значение ===");
        double typical = OrderCalculator.calculateTotalWithProcessing(12, 2490.50, 5);
        System.out.printf("Итог для 12;2490.50;5 = %.2f%n", typical);
        check("TYPICAL 1  12;2490.50;5 -> итог 34570.04", Math.abs(typical - 34570.04) < 0.005);

        // Нижняя граница: включение 1 и 0.01, исключение 0
        System.out.println();
        System.out.println("=== Нижняя граница ===");
        check("LOWER 1  количество 1;1000;10 -> корректно", isValidLine("1;1000;10"));
        check("LOWER 2  цена 1;0.01;0 -> корректно", isValidLine("1;0.01;0"));
        check("LOWER 3  цена 1;0;0 -> ошибка", !isValidLine("1;0;0"));

        // Верхняя граница: включение максимума, исключение значения сразу за ним
        System.out.println();
        System.out.println("=== Верхняя граница ===");
        check("UPPER 1  количество 10000;1000;10 -> корректно", isValidLine("10000;1000;10"));
        check("UPPER 2  количество 10001;1000;10 -> ошибка", !isValidLine("10001;1000;10"));
        check("UPPER 3  цена 10;5000000;10 -> корректно", isValidLine("10;5000000;10"));
        check("UPPER 4  цена 10;5000000.01;10 -> ошибка", !isValidLine("10;5000000.01;10"));
        check("UPPER 5  скидка 10;1000;30 -> корректно", isValidLine("10;1000;30"));
        check("UPPER 6  скидка 10;1000;31 -> ошибка", !isValidLine("10;1000;31"));

        // Пустое или отсутствующее значение: контролируемая ошибка
        System.out.println();
        System.out.println("=== Пустое или отсутствующее значение ===");
        check("EMPTY 1  10;;5 -> ошибка", parseError("10;;5").startsWith("пустое значение"));
        check("EMPTY 2  10;100 -> ошибка", parseError("10;100").startsWith("ожидалось 3 значения"));

        // Значение неверного формата: контролируемая ошибка
        System.out.println();
        System.out.println("=== Неверный формат ===");
        check("FORMAT 1  abc;100;5 -> ошибка", parseError("abc;100;5").startsWith("неверный формат"));
        check("FORMAT 2  10;1000;ten -> ошибка", parseError("10;1000;ten").startsWith("неверный формат"));

        // Повтор или конфликт данных: поведение согласно правилам
        System.out.println();
        System.out.println("=== Повтор и конфликт ===");
        double first = OrderCalculator.calculateTotalWithProcessing(12, 2490.50, 5);
        double second = OrderCalculator.calculateTotalWithProcessing(12, 2490.50, 5);
        check("REPEAT 1  повторный расчёт 12;2490.50;5 даёт тот же итог", first == second);
        // 20 x 100 = 2000; скидка 30% -> 1400; НДС 20% -> 1680; 20 шт. -> обработка бесплатна
        check("CONFLICT 1  20;100;30: максимальная скидка и бесплатная обработка вместе = 1680",
                Math.abs(OrderCalculator.calculateTotalWithProcessing(20, 100.0, 30.0) - 1680.0) < 0.001);

        // Проверка варианта 3: порог бесплатной обработки 20 шт.
        System.out.println();
        System.out.println("=== Вариант 3 ===");
        check("VARIANT 1  19 шт. -> обработка 500", OrderCalculator.calculateProcessingFee(19) == 500.0);
        check("VARIANT 2  20 шт. -> обработка 0", OrderCalculator.calculateProcessingFee(20) == 0.0);
        check("VARIANT 3  10 x 100 без скидки -> 1700",
                Math.abs(OrderCalculator.calculateTotalWithProcessing(10, 100.0, 0.0) - 1700.0) < 0.001);

        System.out.println();
        System.out.println("Итого: " + (passed + failed) + " проверок, PASS: " + passed + ", FAIL: " + failed);
    }

    // Разбирает строку количество;цена;скидка, как в data/sample.csv
    private static double[] parseLine(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("ожидалось 3 значения, получено " + parts.length);
        }
        for (String part : parts) {
            if (part.isBlank()) {
                throw new IllegalArgumentException("пустое значение в строке " + line);
            }
        }
        try {
            int quantity = Integer.parseInt(parts[0].trim());
            double unitPrice = Double.parseDouble(parts[1].trim());
            double discount = Double.parseDouble(parts[2].trim());
            return new double[] {quantity, unitPrice, discount};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("неверный формат: " + line);
        }
    }

    // Строку можно разобрать и данные проходят isValid
    private static boolean isValidLine(String line) {
        double[] v = parseLine(line);
        return OrderCalculator.isValid((int) v[0], v[1], v[2]);
    }

    // Текст контролируемой ошибки разбора или "OK"
    private static String parseError(String line) {
        try {
            parseLine(line);
            return "OK";
        } catch (IllegalArgumentException e) {
            System.out.println("  " + line + " -> " + e.getMessage());
            return e.getMessage();
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
