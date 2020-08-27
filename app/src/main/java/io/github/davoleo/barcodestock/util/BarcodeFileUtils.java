package io.github.davoleo.barcodestock.util;

import android.app.Activity;
import android.widget.Toast;
import io.github.davoleo.barcodestock.R;
import io.github.davoleo.barcodestock.barcode.Barcode;
import io.github.davoleo.barcodestock.barcode.VAT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 11/10/2019 / 20:42
 * Class: BarcodeFileUtils
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class BarcodeFileUtils {

    public static final char SEPARATOR_CHAR = '§';
    public static final String NO_VAT = "NoVAT";
    public static final String EMPTY_DESC = "///";

    public static void writeToFile(Activity activity, Barcode barcode) {

        FileWriter writer;

        try {
            writer = new FileWriter(buildFilePath(activity), true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter
                    .append(String.valueOf(barcode.getCode())).append(SEPARATOR_CHAR)
                    .append(barcode.getTitle()).append(SEPARATOR_CHAR);

            //Encode New Lines
            bufferedWriter.append(barcode.getDescription().replace("\n", "<NL>"));
            bufferedWriter.append(SEPARATOR_CHAR);
            bufferedWriter.append(String.valueOf(barcode.getPrice()));
            bufferedWriter.append(SEPARATOR_CHAR);
            if (barcode.getVat() != null)
                bufferedWriter.append(String.valueOf(barcode.getVat().getValue())).append("\n");
            else
                bufferedWriter.append(NO_VAT).append("\n");

            bufferedWriter.close();
            writer.close();
            //Toast.makeText(activity.getApplicationContext(), "Successfully Registered a new Barcode!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void overwriteListToFile(Activity activity, List<Barcode> barcodeList) {

        FileWriter writer;

        try {
            writer = new FileWriter(buildFilePath(activity), false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            for (Barcode barcode : barcodeList) {
                bufferedWriter
                        .append(String.valueOf(barcode.getCode())).append(SEPARATOR_CHAR)
                        .append(barcode.getTitle()).append(SEPARATOR_CHAR)
                        .append(barcode.getDescription()).append(SEPARATOR_CHAR)
                        .append(String.valueOf(barcode.getPrice())).append(SEPARATOR_CHAR);
                if (barcode.getVat() != null)
                    bufferedWriter.append(String.valueOf(barcode.getVat().getValue())).append("\n");
                else
                    bufferedWriter.append(NO_VAT).append("\n");;
            }

            bufferedWriter.close();
            writer.close();
            //Toast.makeText(activity.getApplicationContext(), "Successfully overwrote new Barcode List!", Toast.LENGTH_SHORT).show();
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
                //Split the different fields
                barcodeInfo = currentLine.split(String.valueOf(SEPARATOR_CHAR));
                //Restore New Lines
                String bDesc = barcodeInfo[2].replace("<NL>", "\n");
                //Replace the encoded empty desc with an actual empty string
                bDesc = bDesc.equals(EMPTY_DESC) ? "" : bDesc;

                VAT vat = barcodeInfo[4].equals(NO_VAT) ? null : VAT.byValue(Integer.parseInt(barcodeInfo[4]));

                Barcode barcode = new Barcode(
                        Long.parseLong(barcodeInfo[0]),
                        barcodeInfo[1],
                        bDesc,
                        Float.parseFloat(barcodeInfo[3]),
                        vat);
                barcodeList.add(barcode);
            }

            bufferedReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(activity, "Warning: You have some old incompatible products in your device!", Toast.LENGTH_SHORT).show();
            Toast.makeText(activity, "Removing all the old products from the storage...", Toast.LENGTH_LONG).show();
            clearBarcodes(activity);
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
        return activity.getApplicationContext().getFilesDir().getPath() + "/" + activity.getString(R.string.barcode_list_filename) + ".txt";
    }

}
