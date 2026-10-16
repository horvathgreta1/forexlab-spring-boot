package com.example.beadando;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalculatorService calculatorService;

    // --- JUNIT UNIT TESZTEK (Üzleti logika ellenőrzése) ---

    @Test
    void testAdd() {
        assertEquals(15.0, calculatorService.calculate(10.0, 5.0, "add"));
    }

    @Test
    void testSubtract() {
        assertEquals(5.0, calculatorService.calculate(10.0, 5.0, "sub"));
    }

    @Test
    void testMultiply() {
        assertEquals(50.0, calculatorService.calculate(10.0, 5.0, "mul"));
    }

    @Test
    void testDivide() {
        assertEquals(2.0, calculatorService.calculate(10.0, 5.0, "div"));
    }

    @Test
    void testDivideByZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculatorService.calculate(10.0, 0.0, "div");
        });
    }

    // --- MOCKMVC INTEGRÁCIÓS TESZTEK (Webes űrlap és routing ellenőrzése) ---

    @Test
    void testGetCalculatorPage() throws Exception {
        mockMvc.perform(get("/calculator"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().attributeExists("calculatorForm"));
    }

    @Test
    void testPostCalculateSuccess() throws Exception {
        mockMvc.perform(post("/calculator")
                        .param("num1", "20")
                        .param("num2", "4")
                        .param("operation", "div"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().attribute("result", 5.0));
    }

    @Test
    void testPostCalculateDivisionByZero() throws Exception {
        mockMvc.perform(post("/calculator")
                        .param("num1", "20")
                        .param("num2", "0")
                        .param("operation", "div"))
                .andExpect(status().isOk())
                .andExpect(view().name("calculator"))
                .andExpect(model().attributeExists("error"));
    }
}
