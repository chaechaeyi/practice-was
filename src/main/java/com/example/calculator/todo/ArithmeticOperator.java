package com.example.calculator.todo;

import com.example.calculator.domain.PositiveNumber;

public interface ArithmeticOperator {
    boolean supports(String operator);

    int calculate(final PositiveNumber operand1, final PositiveNumber operand2);
}
