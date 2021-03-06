package com.mukera.sheket.client.utils;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by fuad on 5/20/16.
 */
public class UnitsOfMeasurement {
    public static final int UNIT_PCS = 0;
    public static final int UNIT_GRAM = 1;
    public static final int UNIT_KG = 2;
    public static final int UNIT_TON = 3;
    public static final int UNIT_ML = 4;
    public static final int UNIT_LITER = 5;
    public static final int UNIT_CM = 6;
    public static final int UNIT_M = 7;
    public static final int UNIT_M_2 = 8;

    public static final int NUM_UNITS = 9;

    public static final String[] UNIT_SYMBOLS = new String[] {
            "pcs",
            "g", "kg", "ton",
            "ml", "l",
            "cm", "m",
            "m2"
    };

    public static ArrayList<String> getAllUnits() {
        ArrayList<String> units = new ArrayList<>();
        for (int i = 0; i < NUM_UNITS; i++) {
            units.add(getUnitSymbol(i));
        }
        return units;
    }
    public static String getUnitSymbol(int unit) {
        return UNIT_SYMBOLS[unit];
    }
}
