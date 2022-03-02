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
		System.out.println("solveNumericExpression_Minus");
		String expression = "10 - 10.0";
		double expResult = 0.0;
		double result = FunctionSolver.solveNumericExpression(expression);
		assertEquals(expResult, result, 0.0);
	}
	
	@Test
	public void testSolveNumericExpression_Add() throws Exception {
		System.out.println("solveNumericExpression_Add");
		String expression = "-10 + 10.0";
		double expResult = 0.0;
		double result = FunctionSolver.solveNumericExpression(expression);
		assertEquals(expResult, result, 0.0);
	}
	
	@Test
	public void testSolveNumericExpression_ExceptionIfNotAnExpression()  {
		System.out.println("solveNumericExpression_ExceptionIfNotAnExpression");
		String expression = "A-10 + 10.0";
		try{
			FunctionSolver.solveNumericExpression(expression);
			fail("shoud throw an exception with this expression "+expression);
		}
		catch (Exception e ){
			System.out.println("OK : exception : "+e.getMessage());
		}
	}
}
