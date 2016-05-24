package com.github.morimotor.gardenapp;

import com.squareup.otto.Bus;

public class BusHolder {

    private static Bus sBus = new Bus();

    public static Bus get() {
        return sBus;
    }

}