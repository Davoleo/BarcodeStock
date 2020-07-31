/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 31/07/2020 / 18:09
 * Class: GraphicUtils
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.util;

import android.graphics.Rect;
import android.graphics.RectF;

public class GraphicUtils {

    public static RectF scale(Rect rect, float multiplier) {
        float left = rect.left / multiplier;
        float top = rect.top / multiplier;
        float right = rect.right * multiplier;
        float bottom = rect.bottom * multiplier;
        return new RectF(left, top, right, bottom);
    }

}
