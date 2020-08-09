/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 09/08/2020 / 17:29
 * Class: VAT
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.barcode;

public enum VAT {

    _4(4),
    _10(10),
    _22(22);

    private final int value;

    VAT(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value + "%";
    }

    public static VAT byValue(int value) {
        return VAT.valueOf("_" + value);
    }
}
