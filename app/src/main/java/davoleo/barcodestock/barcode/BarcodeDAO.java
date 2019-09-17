package davoleo.barcodestock.barcode;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 30/08/2019 / 12:51
 * Interface: BarcodeDAO
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

@Dao
public interface BarcodeDAO {

    @Query("SELECT * FROM `barcode-list`")
    List<Barcode> getAll();

    @Query("SELECT * FROM `barcode-list` WHERE product_title IN (:name)")
    List<Barcode> loadAllByName(String name);

    @Insert
    void insert(Barcode barcode);

    @Delete
    void delete(Barcode barcode);

}
