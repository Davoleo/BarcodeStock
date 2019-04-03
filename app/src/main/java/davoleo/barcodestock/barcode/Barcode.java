package davoleo.barcodestock.barcode;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:19
 * Class: Barcode
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class Barcode {

    private int code;
    private String title;
    private String description;
    private int price;

    public Barcode(int code, String title, String description, int price)
    {
        this.code = code;
        this.title = title;
        this.description = description;
        this.price = price;
    }
}
