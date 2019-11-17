package com.sse.iamhere.Utils;

import android.graphics.Color;

/*
* This enum contains colors that are used as the background of parties item (circle with member count)
* The colors are randomized on purpose
* */
public enum MaterialColors700 {
    Teal("00796B"),
    Red("d32f2f"),
    Cyan("0097A7"),
    Amber("FFA000"),
    Brown("5D4037"),
    Yellow("FBC02D"),
    Light_Blue("0288D1"),
    Deep_Orange("E64A19"),
    Lime("AFB42B"),
    Blue("1976D2"),
    Green("388E3C"),
    Deep_Purple("512DA8"),
    Purple("7B1FA2"),
    Indigo("303F9F"),
    Pink("C2185B"),
    Grey("616161"),
    Orange("F57C00"),
    Light_Green("689F38"),
    Blue_Grey("455A64");


    private final String value;
    MaterialColors700(final String value) {
        this.value = value;
    }

    public int toInt() {
        return Color.parseColor("#" + value);
    }
    public static int[] toArr() {
        int[] returnArr = new int[MaterialColors700.values().length];

        int index = 0;
        for (MaterialColors700 c : values()) {
            returnArr[index++] = c.toInt();
        }

        return returnArr;
    }

}
