/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 17/07/2020 / 19:33
 * Class: BarcodeGraphic
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.scanner;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.google.mlkit.vision.barcode.Barcode;
import org.jetbrains.annotations.NotNull;

public class BarcodeGraphic extends GraphicOverlay.Graphic {

    private static final int TEXT_COLOR = Color.BLACK;
    private static final int MARKER_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54F;
    private static final float STROKE_WIDTH = 4F;

    private final Paint rectPaint;
    private final Paint barcodePaint;
    private final Barcode barcode;
    private final Paint labelPaint;

    BarcodeGraphic(GraphicOverlay overlay, Barcode barcode) {
        super(overlay);

        this.barcode = barcode;

        rectPaint = new Paint();
        rectPaint.setColor(MARKER_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        barcodePaint = new Paint();
        barcodePaint.setColor(TEXT_COLOR);
        barcodePaint.setTextSize(TEXT_SIZE);

        labelPaint = new Paint();
        labelPaint.setColor(MARKER_COLOR);
        labelPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NotNull Canvas canvas) {
        if (barcode == null) {
            throw new IllegalStateException("Trying to draw a null barcode");
        }

        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = overlay.translateX(rect.left);
        rect.top = overlay.translateY(rect.top);
        rect.right = overlay.translateX(rect.right);
        rect.bottom = overlay.translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        float lineHeight = TEXT_SIZE + (2 * STROKE_WIDTH);
        float textWidth = barcodePaint.measureText(barcode.getRawValue());

        canvas.drawRect(
                rect.left - STROKE_WIDTH,
                rect.top - lineHeight,
                rect.left + textWidth + (2 * STROKE_WIDTH),
                rect.top,
                labelPaint
        );

        canvas.drawText(barcode.getRawValue(), rect.left, rect.top - STROKE_WIDTH, barcodePaint);
    }
}
