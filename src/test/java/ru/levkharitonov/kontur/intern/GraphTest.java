package ru.levkharitonov.kontur.intern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * This set of tests checks simple measuring units conversions
 * and correctness of conversion graph building
 */
public class GraphTest {
    private final String[] FROMS = {"м", "мм", "км", "час", "мин"};
    private final String[] TOS = {"см", "м", "м", "мин", "с"};
    private final Double[] RATES = {100., 0.001, 1000., 60., 60.};
    private final int SIZE = 5;

    private Graph conversions;

    @Before
    public void setUp() {
        conversions = new Graph();
        for (int i = 0; i < SIZE; i++) {
            conversions.addRecord(FROMS[i], TOS[i], RATES[i]);
        }
    }

    @Test
    public void getRate() {
        Double[] expected = new Double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            expected[i] = conversions.getRate(FROMS[i], TOS[i]);
        }
        Assert.assertArrayEquals(expected, RATES);
    }

    @Test
    public void getRate_BACKWARDS() {
        Double[] expected = new Double[SIZE];
        Double[] backwardsRates = new Double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            backwardsRates[i] = 1/RATES[i];
            expected[i] = conversions.getRate(TOS[i], FROMS[i]);
        }
        Assert.assertArrayEquals(expected, backwardsRates);
    }

    @Test
    public void getRate_TRAVERSE() {
        Double actual = RATES[3] * RATES[4];
        Double expected = conversions.getRate(FROMS[3], TOS[4]);
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRate_UNKNOWN_UNIT() {
        conversions.getRate("a", "b");
    }

    @Test
    public void getRate_NO_CONVERSION() {
        Assert.assertNull(conversions.getRate(FROMS[0], TOS[4]));
    }
}