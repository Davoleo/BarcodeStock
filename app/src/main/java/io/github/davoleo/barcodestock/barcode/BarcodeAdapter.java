package io.github.davoleo.barcodestock.barcode;

import android.app.Activity;
import android.content.Context;
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

    private Activity context;
    private List<Barcode> barcodeList;

    private static LayoutInflater inflater;

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

        Barcode selectedBarcode = barcodeList.get(position);

        textViewTitle.setText(selectedBarcode.getTitle());
        textViewDesc.setText(selectedBarcode.getDescription());
        textViewPrice.setText("€" + String.format("%.2f", selectedBarcode.getPrice()));
        textViewCode.setText(Long.toString(selectedBarcode.getCode()));

        return itemView;
    }
}
