package com.example.beadando;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    @Autowired
    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/calculator")
    public String showCalculator(Model model) {
        model.addAttribute("calculatorForm", new CalculatorForm());
        return "calculator";
    }

    @PostMapping("/calculator")
    public String calculate(@ModelAttribute CalculatorForm form, Model model) {
        if (form.getNum1() == null || form.getNum2() == null) {
            model.addAttribute("error", "Kérjük, mindkét számot adja meg!");
            model.addAttribute("calculatorForm", form);
            return "calculator";
        }

        try {
            double result = calculatorService.calculate(form.getNum1(), form.getNum2(), form.getOperation());
            model.addAttribute("result", result);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("calculatorForm", form);
        return "calculator";
    }
}
