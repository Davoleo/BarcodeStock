package davoleo.barcodestock.util;

import android.app.Activity;
import android.widget.Toast;
import davoleo.barcodestock.R;
import davoleo.barcodestock.barcode.Barcode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 11/10/2019 / 20:42
 * Class: WriteUtils
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class BarcodeFileUtils {

    public static void writeToFile(Activity activity, Barcode barcode) {

        FileWriter writer = null;

        try {
            writer = new FileWriter(buildFilePath(activity), true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.append(barcode.getCode() + "§").append(barcode.getTitle() + "§").append(barcode.getDescription() + "§").append(barcode.getPrice() + "\n");

            bufferedWriter.close();
            writer.close();
            Toast.makeText(activity.getApplicationContext(), "Successfully Registered a new Barcode!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<Barcode> readAll(Activity activity) {

        checkOrCreateFile(activity);

        FileReader reader = null;
        try {
            reader = new FileReader(buildFilePath(activity));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = new BufferedReader(reader);
        List<Barcode> barcodeList = new ArrayList<>();

        String currentLine;
        String[] barcodeInfo;

        try {

            while ((currentLine = bufferedReader.readLine()) != null) {
                barcodeInfo = currentLine.split("§");
                Barcode barcode = new Barcode(Long.parseLong(barcodeInfo[0]), barcodeInfo[1], barcodeInfo[2], Float.parseFloat(barcodeInfo[3]));
                barcodeList.add(barcode);
            }

            bufferedReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return barcodeList;
    }

    public static void clearBarcodes(Activity activity) {
        try {
            FileWriter writer = new FileWriter(buildFilePath(activity), false);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkOrCreateFile(Activity activity) {
        String filePath = buildFilePath(activity);
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Toast.makeText(activity.getApplicationContext(), "Error during file creation!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static String buildFilePath(Activity activity) {
        return activity.getApplicationContext().getFilesDir().getPath() + "/" + activity.getString(R.string.barcode_list_filename);
    }

}
