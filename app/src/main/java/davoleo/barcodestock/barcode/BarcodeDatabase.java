package davoleo.barcodestock.barcode;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 30/08/2019 / 12:48
 * Class: BarcodeDatabase
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

@Database(entities = Barcode.class, version = 1)
public abstract class BarcodeDatabase extends RoomDatabase {

    public abstract BarcodeDAO barcodeDAO();

}
