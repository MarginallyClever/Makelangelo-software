package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MachineCoordinateTransformTest {
	@BeforeAll
	public static void init() {
		PreferencesHelper.start();
	}

	@Test
	public void mapsAllNineStartingPositionsToPaperEdges() {
		PlotterSettings settings = new PlotterSettings();
		Paper paper = new Paper();
		paper.setPaperSize(400, 600, 20, -30);

		double[] expectedX = {-180, 20, 220, -180, 20, 220, -180, 20, 220};
		double[] expectedY = {270, 270, 270, -30, -30, -30, -330, -330, -330};
		for (int index = 0; index < expectedX.length; index++) {
			settings.setInteger(PlotterSettings.STARTING_POS_INDEX, index);
			Point2d origin = MachineCoordinateTransform.fromSettings(settings, paper).getOrigin();
			assertEquals(expectedX[index], origin.x);
			assertEquals(expectedY[index], origin.y);
		}
	}

	@Test
	public void includesPaperCenterOffsetAndIgnoresMarginsAndMachineLimits() {
		PlotterSettings settings = new PlotterSettings();
		settings.setDouble(PlotterSettings.LIMIT_LEFT, 10);
		settings.setDouble(PlotterSettings.LIMIT_RIGHT, 10);
		settings.setDouble(PlotterSettings.LIMIT_BOTTOM, 50);
		settings.setDouble(PlotterSettings.LIMIT_TOP, -50);
		settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 4);
		Paper paper = new Paper();
		paper.setPaperSize(400, 600, 20, -30);
		paper.setPaperMargin(0.1);

		MachineCoordinateTransform transform = MachineCoordinateTransform.fromSettings(settings, paper);
		Point2d applicationPosition = new Point2d(25, -15);
		Point2d machinePosition = transform.applicationToMachine(applicationPosition);

		assertEquals(20, transform.getOrigin().x);
		assertEquals(-30, transform.getOrigin().y);
		assertEquals(5, machinePosition.x);
		assertEquals(15, machinePosition.y);
		assertEquals(applicationPosition.x, transform.machineToApplication(machinePosition).x);
		assertEquals(applicationPosition.y, transform.machineToApplication(machinePosition).y);
		assertNotSame(applicationPosition, machinePosition);
	}

	@Test
	public void symmetricDefaultCenterLeavesCoordinatesUnchanged() {
		MachineCoordinateTransform transform = MachineCoordinateTransform.fromSettings(new PlotterSettings());
		Point2d position = new Point2d(12.5, -34.5);
		Point2d machinePosition = transform.applicationToMachine(position);

		assertEquals(position.x, machinePosition.x);
		assertEquals(position.y, machinePosition.y);
		assertEquals(0, transform.getOrigin().x);
		assertEquals(0, transform.getOrigin().y);
	}

	@Test
	public void rejectsInvalidPositionIndexAndPaperDimensions() {
		PlotterSettings settings = new PlotterSettings();
		settings.setInteger(PlotterSettings.STARTING_POS_INDEX, -1);
		assertThrows(IllegalArgumentException.class, () -> MachineCoordinateTransform.fromSettings(settings, new Paper()));
		settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 9);
		assertThrows(IllegalArgumentException.class, () -> MachineCoordinateTransform.fromSettings(settings, new Paper()));

		settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 4);
		Paper invalidPaper = new Paper();
		invalidPaper.setPaperSize(0, 600, 0, 0);
		assertThrows(IllegalArgumentException.class, () -> MachineCoordinateTransform.fromSettings(settings, invalidPaper));
		invalidPaper.setPaperSize(Double.NaN, 600, 0, 0);
		assertThrows(IllegalArgumentException.class, () -> MachineCoordinateTransform.fromSettings(settings, invalidPaper));
		invalidPaper.setPaperSize(400, 600, Double.POSITIVE_INFINITY, 0);
		assertThrows(IllegalArgumentException.class, () -> MachineCoordinateTransform.fromSettings(settings, invalidPaper));
	}

	@Test
	public void rejectsNonFinitePositionsAndOverflow() {
		PlotterSettings settings = new PlotterSettings();
		Paper paper = new Paper();
		paper.setPaperSize(Double.MAX_VALUE, 100, 0, 0);
		settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 0);
		MachineCoordinateTransform transform = MachineCoordinateTransform.fromSettings(settings, paper);

		assertThrows(IllegalArgumentException.class, () -> transform.applicationToMachine(new Point2d(Double.NaN, 0)));
		assertThrows(IllegalArgumentException.class, () -> transform.applicationToMachine(new Point2d(Double.MAX_VALUE, 0)));
		assertThrows(IllegalArgumentException.class, () -> transform.machineToApplication(new Point2d(-Double.MAX_VALUE, 0)));
	}
}