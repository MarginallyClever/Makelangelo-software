/*
 */
package com.marginallyclever.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class FunctionSolverTest {
	
	@Test
	public void testSolveNumericExpression_Minus() throws Exception {
		System.out.println("solveNumericExpression");
		String expression = "10 - 10.0";
		double expResult = 0.0;
		double result = FunctionSolver.solveNumericExpression(expression);
		assertEquals(expResult, result, 0.0);
	}
	
	@Test
	public void testSolveNumericExpression_Add() throws Exception {
		System.out.println("solveNumericExpression");
		String expression = "-10 + 10.0";
		double expResult = 0.0;
		double result = FunctionSolver.solveNumericExpression(expression);
		assertEquals(expResult, result, 0.0);
	}

}
