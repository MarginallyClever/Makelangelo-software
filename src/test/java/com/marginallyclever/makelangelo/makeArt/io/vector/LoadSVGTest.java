package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Test;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
import static org.junit.jupiter.api.Assertions.*;

public class LoadSVGTest {

    @Test
    public void canLoad() {
        // given
        TurtleLoader loader = new LoadSVG();

        // then
        assertTrue(loader.canLoad("file.svg"));
        assertFalse(loader.canLoad("file.txt"));
    }

    @Test
    public void load_line() throws Exception {
        verifyLoadSvg("/svg/line.svg", "/svg/line_expected.txt");
    }

    @Test
    public void throwException() throws Exception {
        // given
        TurtleLoader loader = new LoadSVG();

        // then
        assertThrows(NullPointerException.class, () -> {
            loader.load(LoadSVGTest.class.getResourceAsStream("/doesNotExist"));
        }, "Input stream is null");
    }


    @Test
    public void load_circle() throws Exception {
        verifyLoadSvg("/svg/circle.svg", "/svg/circle_expected.txt");
    }

    @Test
    public void load_rectangle() throws Exception {
        verifyLoadSvg("/svg/rect.svg", "/svg/rect_expected.txt");
    }

    @Test
    public void load_multi_shapes1() throws Exception {
        verifyLoadSvg("/svg/multi_shapes1.svg", "/svg/multi_shapes1_expected.txt");
    }

    @Test
    public void load_multi_shapes2() throws Exception {
        verifyLoadSvg("/svg/multi_shapes2.svg", "/svg/multi_shapes2_expected.txt");
    }

    @Test
    public void load_multi_shapes3() throws Exception {
        verifyLoadSvg("/svg/multi_shapes_ignatus1.svg", "/svg/multi_shapes_ignatus1_expected.txt");
    }

    @Test
    public void load_multi_shapes4() throws Exception {
        verifyLoadSvg("/svg/multi_shapes_ignatus2.svg", "/svg/multi_shapes_ignatus2_expected.txt");
    }

    @Test
    public void load_multi_shapes5() throws Exception {
        verifyLoadSvg("/svg/multi_shapes_ying-yang.svg", "/svg/multi_shapes_ying-yang_expected.txt");
    }

    @Test
    public void load_bigfile() throws Exception {
        verifyLoadSvg("/svg/eule.svg", "/svg/eule_expected.txt");
    }

    private void verifyLoadSvg(String svgFile, String expectedFile) throws Exception {
        // given
        TurtleLoader loader = new LoadSVG();

        // when
        Turtle turtle = loader.load(LoadSVGTest.class.getResourceAsStream(svgFile));

        // then
        assertNotNull(turtle);
        assertNotNull(turtle.history);
        assertEquals(readFile(expectedFile), turtle.history.toString());
    }
}