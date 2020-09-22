package io.github.davoleo.barcodestock.barcode;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import io.github.davoleo.barcodestock.R;

import java.util.List;

/*************************************************
 * Author: Davoleo
 * Date / Hour: 17/09/2019 / 18:18
 * Class: BarcodeListAdapter
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2019
 **************************************************/

public class BarcodeAdapter extends BaseAdapter {

    private final Activity context;
    private final List<Barcode> barcodeList;

    private static LayoutInflater inflater;

    //VAT Theme colors - least -> most intense
    private final int[] colors = new int[] {
            0xFFC9C9C9,
            0xFF707070,
            0xFF333333
    };

    public BarcodeAdapter(Activity context, List<Barcode> barcodeList) {
        this.context = context;
        this.barcodeList = barcodeList;
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    public List<Barcode> getData() {
        return barcodeList;
    }

    public void remove(Barcode barcode) {
        barcodeList.remove(barcode);
        notifyDataSetChanged();
    }

    public void remove(long code, String title, String desc, float price) {
        Barcode barcodeToRemove = null;
        for (Barcode barcode : barcodeList) {
            if (barcode.getCode() == code && barcode.getTitle().equals(title) && barcode.getDescription().equals(desc) && barcode.getPrice() == price)
                barcodeToRemove = barcode;
        }

        if (barcodeToRemove != null)
            barcodeList.remove(barcodeToRemove);
    }

    @Override
    public int getCount() {
        return barcodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return barcodeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        itemView = itemView == null ? inflater.inflate(R.layout.barcode_item_layout, null) : itemView;
        TextView textViewTitle = itemView.findViewById(R.id.barcodeTitle);
        TextView textViewDesc = itemView.findViewById(R.id.barcodeDesc);
        TextView textViewPrice = itemView.findViewById(R.id.barcodePrice);
        TextView textViewCode = itemView.findViewById(R.id.barcode);
        TextView textViewVat = itemView.findViewById(R.id.barcodeVat);

        Barcode selectedBarcode = barcodeList.get(position);

        textViewTitle.setText(selectedBarcode.getTitle());

        String description = selectedBarcode.getDescription().equals("///") ? "" : selectedBarcode.getDescription();
        textViewDesc.setText(description);

        textViewPrice.setText("€" + String.format("%.2f", selectedBarcode.getPrice()));
        textViewCode.setText(Long.toString(selectedBarcode.getCode()));

        if (selectedBarcode.getVat() != null)
        {
            textViewVat.setText("VAT: " + selectedBarcode.getVat().toString());

            Drawable drawable = (context.getDrawable(R.drawable.rounded_corners));

            if (drawable instanceof ShapeDrawable)
                ((ShapeDrawable) drawable).getPaint().setColor(colors[selectedBarcode.getVat().ordinal()]);
            else if (drawable instanceof GradientDrawable)
                ((GradientDrawable) drawable).setColor(colors[selectedBarcode.getVat().ordinal()]);

            if (selectedBarcode.getVat() != VAT._4)
                textViewVat.setTextColor(0xFFFFFFFF);
            else
                textViewVat.setTextColor(0xFF000000);

            textViewVat.setBackground(drawable);
        }
        else {
            GradientDrawable drawable = ((GradientDrawable) context.getDrawable(R.drawable.rounded_corners));
            drawable.setAlpha(0);
            textViewVat.setBackground(drawable);
            textViewVat.setText("");
        }

        return itemView;
    }
}
