package davoleo.barcodestock.barcode;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:19
 * Class: Barcode
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

@Entity(tableName = "barcode_list")
public class Barcode {

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "barcode")
    private int code;

    @ColumnInfo(name = "product_title")
    private String title;

    @ColumnInfo(name = "product_desc")
    private String description;

    @ColumnInfo(name = "product_price")
    private float price;

    public Barcode(int code, String title, String description, float price)
    {
        this.code = code;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public int getCode() {
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
}
