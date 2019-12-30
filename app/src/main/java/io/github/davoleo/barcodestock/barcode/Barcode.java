package io.github.davoleo.barcodestock.barcode;

import android.support.annotation.NonNull;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:19
 * Class: Barcode
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class Barcode implements Comparable<Barcode> {

    private long code;
    private String title;
    private String description;
    private float price;

    public static BarcodeFields sortingMethod = BarcodeFields.TITLE;

    public Barcode(long code, String title, String description, float price)
    {
        this.code = code;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public long getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    @Override
    public int compareTo(Barcode o) {
        switch (sortingMethod) {
            case BARCODE:
                return Long.compare(this.getCode(), o.getCode());
            case DESCRIPTION:
                return this.getDescription().compareToIgnoreCase(o.getDescription());
            case PRICE:
                return Float.compare(this.getPrice(), o.getPrice());
            case TITLE:
            default:
                return this.getTitle().compareToIgnoreCase(o.getTitle());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Barcode{ Title: " + title + " | Description: " + description + " | Code: " + code + " | Price: " + price + "}";
    }

    public enum BarcodeFields {
        BARCODE,
        TITLE,
        DESCRIPTION,
        PRICE
    }
}
