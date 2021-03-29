package ru.levkharitonov.kontur.intern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static ru.levkharitonov.kontur.intern.Converter.*;

/*
 * This set of tests checks complex measuring units conversion
 */
public class ConverterTest {
    private final Double VELOCITY_COEF = 3.6;
    private final Double KILOMETER_COEF = 1000.;
    private final String MUL_CHECK = "  A*B   * C*D";
    private final List<String> MUL_CHECK_SEPARATED =
            new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
    private Graph conversions;

    @Before
    public void setUp() throws Exception {
        conversions = new Graph();
        readConversions(conversions, "src/test/test.csv");
    }

    @Test
    public void mulSplit_CHECK() {
        Assert.assertEquals(mulSplit(MUL_CHECK), MUL_CHECK_SEPARATED);
    }

    @Test
    public void convert_COMPLEX() {
        Double actual = VELOCITY_COEF;
        Double expected = convert("м/с","км/ час", conversions);
        Assert.assertEquals(expected, actual);
        actual = 1/VELOCITY_COEF;
        expected = convert("км/ час","м/с", conversions);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void convert_SELF_REDUCING() {
        Double actual = VELOCITY_COEF;
        Double expected = convert("м","км * с / час", conversions);
        Assert.assertEquals(expected, actual);
        actual = KILOMETER_COEF;
        expected = convert("м*км / м","м", conversions);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void convert_EMPTY() {
        Double expected = convert("км / м","", conversions);
        Assert.assertEquals(expected, KILOMETER_COEF);
    }

    @Test
    public void convert_ONE() {
        Double expected = convert("1/м","1/км", conversions);
        Assert.assertEquals(expected, KILOMETER_COEF);
    }

    @Test(expected = ArithmeticException.class)
    public void convert_UNBALANCED_UNITS() {
        convert("с*м/с","км/ час", conversions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convert_UNKNOWN_UNIT() {
        convert("ddd*м/с","км/ час", conversions);
    }

}