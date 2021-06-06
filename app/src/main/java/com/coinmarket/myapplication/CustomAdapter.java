package com.coinmarket.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private ArrayList<DataModel> dataSet;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        TextView name = holder.name;
        TextView symbol = holder.symbol;
        TextView platform = holder.platform;
        TextView price = holder.price;
        TextView address = holder.address;
        ImageView copy = holder.copy;

        name.setText(dataSet.get(position).getName());
        symbol.setText(dataSet.get(position).getSymbol());
        price.setText(dataSet.get(position).getPrice());
        platform.setText(dataSet.get(position).getPlatform());
        address.setText(dataSet.get(position).getAddress());


    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView symbol;
        TextView platform;
        TextView price;
        TextView address;
        ImageView copy;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.Name);
            this.symbol = (TextView) itemView.findViewById(R.id.symbol);
            this.platform = (TextView) itemView.findViewById(R.id.platform);
            this.price = (TextView) itemView.findViewById(R.id.price);
            this.address = (TextView) itemView.findViewById(R.id.address);
            this.copy = (ImageView) itemView.findViewById(R.id.copy);
            copy.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(this.address.getText());
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", this.address.getText());
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(v.getContext(), "Copied address of " + this.name.getText(), Toast.LENGTH_SHORT).show();
        }

    }

    public CustomAdapter(ArrayList<DataModel> dataSet) {
        this.dataSet = dataSet;
    }


}
