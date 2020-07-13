/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 13/07/2020 / 17:03
 * Class: CompareSizeByArea
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.util;

import android.util.Size;

import java.util.Comparator;

public class CompareSizeByArea implements Comparator<Size> {

    @Override
    public int compare(Size o1, Size o2) {
        return Long.signum(o1.getWidth() * o1.getHeight() / o2.getWidth() * o2.getHeight());
    }
}
