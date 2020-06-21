/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 21/06/2020 / 17:14
 * Class: BarcodeComparable
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.barcode;

import java.util.Comparator;

public class BarcodeComparator implements Comparator<Barcode> {

    private Barcode.BarcodeFields comparableField;

    public BarcodeComparator(Barcode.BarcodeFields comparableField) {
        this.comparableField = comparableField;
    }

    @Override
    public int compare(Barcode o1, Barcode o2) {
        switch (comparableField) {
            case BARCODE:
                return Long.compare(o1.getCode(), o2.getCode());
            case DESCRIPTION:
                return o1.getDescription().compareToIgnoreCase(o2.getDescription());
            case PRICE:
                return Float.compare(o1.getPrice(), o2.getPrice());
            case TITLE:
            default:
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public Barcode.BarcodeFields getComparableField() {
        return comparableField;
    }

    public void setCompareBy(Barcode.BarcodeFields comparableField) {
        this.comparableField = comparableField;
    }
}
