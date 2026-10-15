package ru.edu.pr01;

public class OrderCalculator {
    private static final double VAT_RATE = 20.0;
    private static final int FREE_PROCESSING_THRESHOLD = 20;
    private static final double PROCESSING_FEE = 500.0;
    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return quantity > 0
                && quantity <= 10_000
                && unitPrice > 0.0
                && unitPrice <= 5_000_000.0
                && discountPercent >= 0.0
                && discountPercent <= 30.0;
    }
    public static double calculateBase(int quantity, double unitPrice) {
        return quantity * unitPrice;
    }
    public static double applyDiscount(double base, double discountPercent) {
        return base * (1.0 - discountPercent / 100.0);
    }
    public static double calculateVat(double discounted, double vatPercent) {
        return discounted * vatPercent / 100.0;
    }
    public static double calculateTotal(int quantity, double unitPrice, double discountPercent, double vatPercent) {
        double base = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        return discounted + calculateVat(discounted, vatPercent);
    }
    public static double calculateProcessingFee(int quantity) {
        if (quantity >= FREE_PROCESSING_THRESHOLD) {
            return 0.0;
        }
        return PROCESSING_FEE;
    }
    public static double calculateTotalWithProcessing(int quantity, double unitPrice, double discountPercent) {
        return calculateTotal(quantity, unitPrice, discountPercent, VAT_RATE) + calculateProcessingFee(quantity);
    }
}
