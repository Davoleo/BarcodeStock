package davoleo.barcodestock.barcode;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:19
 * Class: Barcode
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class Barcode implements Comparable<Barcode> {

    public int uid;
    private long code;
    private String title;
    private String description;
    private float price;

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
        return this.getTitle().compareToIgnoreCase(o.getTitle());
    }
}
