/*
 */
package com.marginallyclever.makelangelo.plotter.settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Source : https://stackoverflow.com/questions/2605032/is-there-an-eval-function-in-java : https://stackoverflow.com/posts/48251395/revisions
 */
public abstract class FunctionSolver {

	public static double solveNumericExpression(String expression) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return solve(expression, new HashMap<>());
	}

	public static double solveByX(String function, double value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		HashMap<String, Double> values = new HashMap<>();
		values.put("x", value);
		return solveComplexFunction(function, function, values);
	}

	public static double solve(String function, HashMap<String, Double> values) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		return solveComplexFunction(function, function, values);
	}

	private static double solveComplexFunction(String function, String motherFunction, HashMap<String, Double> values) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		int position = 0;
		while (position < function.length()) {
			if (alphabetic.contains("" + function.charAt(position))) {
				if (position == 0 || !alphabetic.contains("" + function.charAt(position - 1))) {
					int endIndex = -1;
					for (int j = position; j < function.length() - 1; j++) {
						if (alphabetic.contains("" + function.charAt(j))
								&& !alphabetic.contains("" + function.charAt(j + 1))) {
							endIndex = j;
							break;
						}
					}
					if (endIndex == -1 & alphabetic.contains("" + function.charAt(function.length() - 1))) {
						endIndex = function.length() - 1;
					}
					if (endIndex != -1) {
						String alphabeticElement = function.substring(position, endIndex + 1);
						if (Arrays.asList(usableMathMethods()).contains(alphabeticElement)) {
							//Start analyzing a Math function
							int closeParenthesisIndex = -1;
							int openedParenthesisquantity = 0;
							int commaIndex = -1;
							for (int j = endIndex + 1; j < function.length(); j++) {
								if (function.substring(j, j + 1).equals("(")) {
									openedParenthesisquantity++;
								} else if (function.substring(j, j + 1).equals(")")) {
									openedParenthesisquantity--;
									if (openedParenthesisquantity == 0) {
										closeParenthesisIndex = j;
										break;
									}
								} else if (function.substring(j, j + 1).equals(",") & openedParenthesisquantity == 0) {
									if (commaIndex == -1) {
										commaIndex = j;
									} else {
										throw new IllegalArgumentException("The argument of math function (which is " + alphabeticElement + ") has too many commas");
									}
								}
							}
							if (closeParenthesisIndex == -1) {
								throw new IllegalArgumentException("The argument of a Math function (which is " + alphabeticElement + ") hasn't got the closing bracket )");
							}
							String functionArgument = function.substring(endIndex + 2, closeParenthesisIndex);
							if (commaIndex != -1) {
								double firstParameter = solveComplexFunction(functionArgument.substring(0, commaIndex), motherFunction, values);
								double secondParameter = solveComplexFunction(functionArgument.substring(commaIndex + 1), motherFunction, values);
								Method mathMethod = Math.class.getDeclaredMethod(alphabeticElement, new Class<?>[]{double.class, double.class});
								mathMethod.setAccessible(true);
								String newKey = getNewKey(values);
								values.put(newKey, (Double) mathMethod.invoke(null, firstParameter, secondParameter));
								function = function.substring(0, position) + newKey
										+ ((closeParenthesisIndex == function.length() - 1) ? ("") : (function.substring(closeParenthesisIndex + 1)));
							} else {
								double firstParameter = solveComplexFunction(functionArgument, motherFunction, values);
								Method mathMethod = Math.class.getDeclaredMethod(alphabeticElement, new Class<?>[]{double.class});
								mathMethod.setAccessible(true);
								String newKey = getNewKey(values);
								values.put(newKey, (Double) mathMethod.invoke(null, firstParameter));
								function = function.substring(0, position) + newKey
										+ ((closeParenthesisIndex == function.length() - 1) ? ("") : (function.substring(closeParenthesisIndex + 1)));
							}
						} else if (!values.containsKey(alphabeticElement)) {
							throw new IllegalArgumentException("Found a group of letters (" + alphabeticElement + ") which is neither a variable nor a Math function: ");
						}
					}
				}
			}
			position++;
		}
		return solveBracketsFunction(function, motherFunction, values);
	}

	private static double solveBracketsFunction(String function, String motherFunction, HashMap<String, Double> values) throws IllegalArgumentException {

		function = function.replace(" ", "");
		String openingBrackets = "([{";
		String closingBrackets = ")]}";
		int parenthesisIndex = 0;
		do {
			int position = 0;
			int openParenthesisBlockIndex = -1;
			String currentOpeningBracket = openingBrackets.charAt(parenthesisIndex) + "";
			String currentClosingBracket = closingBrackets.charAt(parenthesisIndex) + "";
			if (contOccouranceIn(currentOpeningBracket, function) != contOccouranceIn(currentClosingBracket, function)) {
				throw new IllegalArgumentException("Error: brackets are misused in the function " + function);
			}
			while (position < function.length()) {
				if (function.substring(position, position + 1).equals(currentOpeningBracket)) {
					if (position != 0 && !operators.contains(function.substring(position - 1, position))) {
						throw new IllegalArgumentException("Error in function: there must be an operator following a " + currentClosingBracket + " breacket");
					}
					openParenthesisBlockIndex = position;
				} else if (function.substring(position, position + 1).equals(currentClosingBracket)) {
					if (position != function.length() - 1 && !operators.contains(function.substring(position + 1, position + 2))) {
						throw new IllegalArgumentException("Error in function: there must be an operator before a " + currentClosingBracket + " breacket");
					}
					String newKey = getNewKey(values);
					values.put(newKey, solveBracketsFunction(function.substring(openParenthesisBlockIndex + 1, position), motherFunction, values));
					function = function.substring(0, openParenthesisBlockIndex) + newKey
							+ ((position == function.length() - 1) ? ("") : (function.substring(position + 1)));
					position = -1;
				}
				position++;
			}
			parenthesisIndex++;
		} while (parenthesisIndex < openingBrackets.length());
		return solveBasicFunction(function, motherFunction, values);
	}

	private static double solveBasicFunction(String function, String motherFunction, HashMap<String, Double> values) throws IllegalArgumentException {

		if (!firstContainsOnlySecond(function, alphanumeric + operators)) {
			throw new IllegalArgumentException("The function " + function + " is not a basic function");
		}
		if (function.contains("**")
				| function.contains("//")
				| function.contains("--")
				| function.contains("+*")
				| function.contains("+/")
				| function.contains("-*")
				| function.contains("-/")) {
			/*
         * ( -+ , +- , *- , *+ , /- , /+ )> Those values are admitted
			 */
			throw new IllegalArgumentException("Operators are misused in the function");
		}
		function = function.replace(" ", "");
		int position;
		int operatorIndex = 0;
		String currentOperator;
		do {
			currentOperator = operators.substring(operatorIndex, operatorIndex + 1);
			if (currentOperator.equals("*")) {
				currentOperator += "/";
				operatorIndex++;
			} else if (currentOperator.equals("+")) {
				currentOperator += "-";
				operatorIndex++;
			}
			operatorIndex++;
			position = 0;
			while (position < function.length()) {
				if ((position == 0 && !("" + function.charAt(position)).equals("-") && !("" + function.charAt(position)).equals("+") && operators.contains("" + function.charAt(position)))
						|| (position == function.length() - 1 && operators.contains("" + function.charAt(position)))) {
					throw new IllegalArgumentException("Operators are misused in the function");
				}
				if (currentOperator.contains(function.substring(position, position + 1)) & position != 0) {
					int firstTermBeginIndex = position;
					while (firstTermBeginIndex > 0) {
						if ((alphanumeric.contains("" + function.charAt(firstTermBeginIndex))) & (operators.contains("" + function.charAt(firstTermBeginIndex - 1)))) {
							break;
						}
						firstTermBeginIndex--;
					}
					if (firstTermBeginIndex != 0 && (function.charAt(firstTermBeginIndex - 1) == '-' | function.charAt(firstTermBeginIndex - 1) == '+')) {
						if (firstTermBeginIndex == 1) {
							firstTermBeginIndex--;
						} else if (operators.contains("" + (function.charAt(firstTermBeginIndex - 2)))) {
							firstTermBeginIndex--;
						}
					}
					String firstTerm = function.substring(firstTermBeginIndex, position);
					int secondTermLastIndex = position;
					while (secondTermLastIndex < function.length() - 1) {
						if ((alphanumeric.contains("" + function.charAt(secondTermLastIndex))) & (operators.contains("" + function.charAt(secondTermLastIndex + 1)))) {
							break;
						}
						secondTermLastIndex++;
					}
					String secondTerm = function.substring(position + 1, secondTermLastIndex + 1);
					double result;
					switch (function.substring(position, position + 1)) {
						case "*":
							result = solveSingleValue(firstTerm, values) * solveSingleValue(secondTerm, values);
							break;
						case "/":
							result = solveSingleValue(firstTerm, values) / solveSingleValue(secondTerm, values);
							break;
						case "+":
							result = solveSingleValue(firstTerm, values) + solveSingleValue(secondTerm, values);
							break;
						case "-":
							result = solveSingleValue(firstTerm, values) - solveSingleValue(secondTerm, values);
							break;
						case "^":
							result = Math.pow(solveSingleValue(firstTerm, values), solveSingleValue(secondTerm, values));
							break;
						default:
							throw new IllegalArgumentException("Unknown operator: " + currentOperator);
					}
					String newAttribute = getNewKey(values);
					values.put(newAttribute, result);
					function = function.substring(0, firstTermBeginIndex) + newAttribute + function.substring(secondTermLastIndex + 1, function.length());
					deleteValueIfPossible(firstTerm, values, motherFunction);
					deleteValueIfPossible(secondTerm, values, motherFunction);
					position = -1;
				}
				position++;
			}
		} while (operatorIndex < operators.length());
		return solveSingleValue(function, values);
	}

	private static double solveSingleValue(String singleValue, HashMap<String, Double> values) throws IllegalArgumentException {

		if (isDouble(singleValue)) {
			return Double.parseDouble(singleValue);
		} else if (firstContainsOnlySecond(singleValue, alphabetic)) {
			return getValueFromVariable(singleValue, values);
		} else if (firstContainsOnlySecond(singleValue, alphanumeric + "-+")) {
			String[] composition = splitByLettersAndNumbers(singleValue);
			if (composition.length != 2) {
				throw new IllegalArgumentException("Wrong expression: " + singleValue);
			} else {
				if (composition[0].equals("-")) {
					composition[0] = "-1";
				} else if (composition[1].equals("-")) {
					composition[1] = "-1";
				} else if (composition[0].equals("+")) {
					composition[0] = "+1";
				} else if (composition[1].equals("+")) {
					composition[1] = "+1";
				}
				if (isDouble(composition[0])) {
					return Double.parseDouble(composition[0]) * getValueFromVariable(composition[1], values);
				} else if (isDouble(composition[1])) {
					return Double.parseDouble(composition[1]) * getValueFromVariable(composition[0], values);
				} else {
					throw new IllegalArgumentException("Wrong expression: " + singleValue);
				}
			}
		} else {
			throw new IllegalArgumentException("Wrong expression: " + singleValue);
		}
	}

	private static double getValueFromVariable(String variable, HashMap<String, Double> values) throws IllegalArgumentException {

		Double val = values.get(variable);
		if (val == null) {
			throw new IllegalArgumentException("Unknown variable: " + variable);
		} else {
			return val;
		}
	}

	/*
 * FunctionSolver help tools:
 * 
	 */
	private static final String alphabetic = "abcdefghilmnopqrstuvzwykxy";
	private static final String numeric = "0123456789.";
	private static final String alphanumeric = alphabetic + numeric;
	private static final String operators = "^*/+-"; //--> Operators order in important!

	private static boolean firstContainsOnlySecond(String firstString, String secondString) {

		for (int j = 0; j < firstString.length(); j++) {
			if (!secondString.contains(firstString.substring(j, j + 1))) {
				return false;
			}
		}
		return true;
	}

	private static String getNewKey(HashMap<String, Double> hashMap) {

		String alpha = "abcdefghilmnopqrstuvzyjkx";
		for (int j = 0; j < alpha.length(); j++) {
			String k = alpha.substring(j, j + 1);
			if (!hashMap.containsKey(k) & !Arrays.asList(usableMathMethods()).contains(k)) {
				return k;
			}
		}
		for (int j = 0; j < alpha.length(); j++) {
			for (int i = 0; i < alpha.length(); i++) {
				String k = alpha.substring(j, j + 1) + alpha.substring(i, i + 1);
				if (!hashMap.containsKey(k) & !Arrays.asList(usableMathMethods()).contains(k)) {
					return k;
				}
			}
		}
		throw new NullPointerException();
	}

	public static String[] usableMathMethods() {

		/*
     *  Only methods that:
     *  return a double type
     *  present one or two parameters (which are double type)
		 */
		Method[] mathMethods = Math.class.getDeclaredMethods();
		ArrayList<String> usableMethodsNames = new ArrayList<>();
		for (Method method : mathMethods) {
			boolean usable = true;
			int argumentsCounter = 0;
			Class<?>[] methodParametersTypes = method.getParameterTypes();
			for (Class<?> parameter : methodParametersTypes) {
				if (!parameter.getSimpleName().equalsIgnoreCase("double")) {
					usable = false;
					break;
				} else {
					argumentsCounter++;
				}
			}
			if (!method.getReturnType().getSimpleName().toLowerCase().equals("double")) {
				usable = false;
			}
			if (usable & argumentsCounter <= 2) {
				usableMethodsNames.add(method.getName());
			}
		}
		return usableMethodsNames.toArray(new String[usableMethodsNames.size()]);
	}

	private static boolean isDouble(String number) {
		try {
			Double.parseDouble(number);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static String[] splitByLettersAndNumbers(String val) {
		if (!firstContainsOnlySecond(val, alphanumeric + "+-")) {
			throw new IllegalArgumentException("Wrong passed value: <<" + val + ">>");
		}
		ArrayList<String> response = new ArrayList<>();
		String searchingFor;
		int lastIndex = 0;
		if (firstContainsOnlySecond("" + val.charAt(0), numeric + "+-")) {
			searchingFor = alphabetic;
		} else {
			searchingFor = numeric + "+-";
		}
		for (int j = 0; j < val.length(); j++) {
			if (searchingFor.contains(val.charAt(j) + "")) {
				response.add(val.substring(lastIndex, j));
				lastIndex = j;
				if (searchingFor.equals(numeric + "+-")) {
					searchingFor = alphabetic;
				} else {
					searchingFor = numeric + "+-";
				}
			}
		}
		response.add(val.substring(lastIndex, val.length()));
		return response.toArray(new String[response.size()]);
	}

	private static void deleteValueIfPossible(String val, HashMap<String, Double> values, String function) {
		if (values.get(val) != null & function != null) {
			if (!function.contains(val)) {
				values.remove(val);
			}
		}
	}

	private static int contOccouranceIn(String howManyOfThatString, String inThatString) {
		return inThatString.length() - inThatString.replace(howManyOfThatString, "").length();
	}
}
