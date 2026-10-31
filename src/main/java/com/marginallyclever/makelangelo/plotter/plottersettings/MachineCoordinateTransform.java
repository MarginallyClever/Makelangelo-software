package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.makelangelo.paper.Paper;

import javax.vecmath.Point2d;
import java.util.Objects;

/**
 * Converts positions between the application coordinate frame and the firmware coordinate frame.
 */
public final class MachineCoordinateTransform {
	private final Point2d origin;

	private MachineCoordinateTransform(Point2d origin) {
		this.origin = origin;
	}

	/**
	 * Creates a transform using the selected position on the default paper.
	 * @param settings machine settings
	 * @return a transform for the selected origin
	 * @throws IllegalArgumentException if the selected origin or paper dimensions are invalid
	 */
	public static MachineCoordinateTransform fromSettings(PlotterSettings settings) {
		return fromSettings(settings, new Paper());
	}

	/**
	 * Creates a transform using the selected position on the active paper's outer edges.
	 * @param settings machine settings
	 * @param paper active paper
	 * @return a transform for the selected origin
	 * @throws IllegalArgumentException if the selected origin or paper dimensions are invalid
	 */
	public static MachineCoordinateTransform fromSettings(PlotterSettings settings, Paper paper) {
		Objects.requireNonNull(settings, "settings");
		Objects.requireNonNull(paper, "paper");

		int index = settings.getInteger(PlotterSettings.STARTING_POS_INDEX);
		PlotterSettings.StartingPosition[] positions = PlotterSettings.StartingPosition.values();
		if (index < 0 || index >= positions.length) {
			throw new IllegalArgumentException("Invalid starting position index: " + index);
		}

		int horizontalIndex = positions[index].ordinal() % 3;
		int verticalIndex = positions[index].ordinal() / 3;
		double centerX = paper.getCenterX();
		double centerY = paper.getCenterY();
		double left = centerX + paper.getPaperLeft();
		double right = centerX + paper.getPaperRight();
		double bottom = centerY + paper.getPaperBottom();
		double top = centerY + paper.getPaperTop();
		validatePaper(left, right, bottom, top, centerX, centerY);
		double[] xPositions = {left, midpoint(left, right), right};
		double[] yPositions = {top, midpoint(bottom, top), bottom};
		return new MachineCoordinateTransform(new Point2d(xPositions[horizontalIndex], yPositions[verticalIndex]));
	}

	private static void validatePaper(double left, double right, double bottom, double top, double centerX, double centerY) {
		if (!Double.isFinite(left) || !Double.isFinite(right) || !Double.isFinite(bottom) || !Double.isFinite(top)
				|| !Double.isFinite(centerX) || !Double.isFinite(centerY) || left >= right || bottom >= top) {
			throw new IllegalArgumentException("Paper dimensions and center must be finite and have positive width and height");
		}
	}

	private static double midpoint(double minimum, double maximum) {
		return minimum / 2.0 + maximum / 2.0;
	}

	/**
	 * @return the selected firmware origin expressed in application coordinates
	 */
	public Point2d getOrigin() {
		return new Point2d(origin);
	}

	/**
	 * Converts an absolute application position to an absolute firmware position.
	 * @param applicationPosition application-frame position
	 * @return firmware-frame position
	 */
	public Point2d applicationToMachine(Point2d applicationPosition) {
		validatePosition(applicationPosition);
		Point2d result = new Point2d(applicationPosition.x - origin.x, applicationPosition.y - origin.y);
		validatePosition(result);
		return result;
	}

	/**
	 * Converts an absolute firmware position to an absolute application position.
	 * @param machinePosition firmware-frame position
	 * @return application-frame position
	 */
	public Point2d machineToApplication(Point2d machinePosition) {
		validatePosition(machinePosition);
		Point2d result = new Point2d(machinePosition.x + origin.x, machinePosition.y + origin.y);
		validatePosition(result);
		return result;
	}

	private static void validatePosition(Point2d position) {
		Objects.requireNonNull(position, "position");
		if (!Double.isFinite(position.x) || !Double.isFinite(position.y)) {
			throw new IllegalArgumentException("Position coordinates must be finite");
		}
	}
}