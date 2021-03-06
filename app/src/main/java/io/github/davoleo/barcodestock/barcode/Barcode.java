package io.github.davoleo.barcodestock.barcode;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:19
 * Class: Barcode
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class Barcode {

    private final long code;
    private final String title;
    private final String description;
    private final float price;
    private final VAT vat;

    public Barcode(long code, String title, String description, float price, VAT vat)
    {
        this.code = code;
        this.title = title;
        this.description = description;
        this.price = price;
        this.vat = vat;
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

    @Nullable
    public VAT getVat() {
        return vat;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong("code", code);
        bundle.putString("title", title);
        bundle.putString("desc", description);
        bundle.putFloat("price", price);
        if (vat != null)
            bundle.putInt("vat", vat.getValue());
        return bundle;
    }

    public static Barcode fromBundle(Bundle bundle) {

        VAT vat = bundle.containsKey("vat") ? VAT.byValue(bundle.getInt("vat")) : null;

        return new Barcode(
                bundle.getLong("code"),
                bundle.getString("title"),
                bundle.getString("desc"),
                bundle.getFloat("price"),
                vat
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "Barcode{ Title: "
                + title + " | Description: "
                + description + " | Code: "
                + code + " | Price: "
                + price + " | VAT: "
                + vat.getValue()
                + "}";
    }

    public enum BarcodeFields {
        BARCODE,
        TITLE,
        DESCRIPTION,
        PRICE
    }
}
