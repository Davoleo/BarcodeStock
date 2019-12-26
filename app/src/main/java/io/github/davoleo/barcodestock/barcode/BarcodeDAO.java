package davoleo.barcodestock.barcode;

import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 30/08/2019 / 12:51
 * Interface: BarcodeDAO
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

//@Dao
public interface BarcodeDAO {

//    @Query("SELECT * FROM barcode_list")
    List<Barcode> getAll();

 //   @Query("SELECT * FROM barcode_list WHERE product_title IN (:name)")
    List<Barcode> loadAllByName(String name);

//    @Query("SELECT COUNT(*) FROM barcode_list")
    int getBarcodesCount();

//    @Insert
    void insert(Barcode barcode);

//    @Delete
    void delete(Barcode barcode);

}
