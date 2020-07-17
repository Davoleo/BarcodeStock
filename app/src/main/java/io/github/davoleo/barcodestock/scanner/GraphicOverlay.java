/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 17/07/2020 / 20:01
 * Class: GraphicOverlay
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {

    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();

    // Matrix to transform from image coordinates to overlay view coordinates.
    private final Matrix transformationMatrix = new Matrix();

    private int imageWidth;
    private int imageHeight;

    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private float scaleFactor = 1F;
    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleWidthOffset;
    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleHeightOffset;

    private boolean isImageFlipped;
    private boolean needUpdateTransformation = true;

    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scale(float imagePixel) {
            return imagePixel * overlay.scaleFactor;
        }

        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }

        public boolean isImageFlipped() {
            return overlay.isImageFlipped;
        }

        public float translateX(float x) {
            if (overlay.isImageFlipped) {
                return overlay.getWidth() - (scale(x) - overlay.postScaleWidthOffset);
            } else {
                return scale(x) - overlay.postScaleWidthOffset;
            }
        }

        public float translateY(float y) {
            return scale(y) - overlay.postScaleHeightOffset;
        }

        public Matrix getTransformationMatrix() {
            return overlay.transformationMatrix;
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attributes) {
        super(context, attributes);
        addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> needUpdateTransformation = true);
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isImageFlipped) {
        if (imageHeight > 0 && imageWidth > 0) {
            synchronized (lock) {
                this.imageWidth = imageWidth;
                this.imageHeight = imageHeight;
                this.isImageFlipped = isImageFlipped;
                needUpdateTransformation = true;
            }
            postInvalidate();
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    private void updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0)
            return;

        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) imageWidth / imageHeight;

        postScaleWidthOffset = 0;
        postScaleHeightOffset = 0;

        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = (float) getWidth() / imageWidth;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        }
        else {
            scaleFactor = (float) getHeight() / imageHeight;
            postScaleWidthOffset = ((float) getHeight() / imageAspectRatio - getWidth()) / 2;
        }

        transformationMatrix.reset();
        transformationMatrix.setScale(scaleFactor, scaleFactor);
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);

        if (isImageFlipped) {
            transformationMatrix.postScale(-1F, 1F, getWidth() / 2F, getHeight() / 2F);
        }

        needUpdateTransformation = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            updateTransformationIfNeeded();

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}
