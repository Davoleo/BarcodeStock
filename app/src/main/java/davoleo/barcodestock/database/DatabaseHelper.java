package davoleo.barcodestock.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 29/03/2019 / 16:28
 * Class: DatabaseHelper
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    private void init(SQLiteDatabase database)
    {

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    { }
}
