package com.example.beadando;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public double calculate(double a, double b, String operation) {
        return switch (operation) {
            case "add" -> a + b;
            case "sub" -> a - b;
            case "mul" -> a * b;
            case "div" -> {
                if (b == 0) {
                    throw new IllegalArgumentException("Nullával nem lehet osztani!");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Ismeretlen művelet: " + operation);
        };
    }
}
