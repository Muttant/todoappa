package com.example.todolist;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CongViecAdapter extends BaseAdapter {

    private MainActivity context;
    private int layout;
    private List<CongViec> congViecList;
    private Database database;
    private IHandleCheckBox handleCheckBox;

    public CongViecAdapter(MainActivity context, int layout, List<CongViec> congViecList, Database database, IHandleCheckBox handleCheckBox) {
        this.context = context;
        this.layout = layout;
        this.congViecList = congViecList;
        this.database = database;
        this.handleCheckBox = handleCheckBox;
    }

    @Override
    public int getCount() {
        return congViecList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        CheckBox checkBox;
        TextView txtTen;
        TextView txtDate;
        TextView txtTime;
        ImageView imgDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);

            holder.txtTen = (TextView) convertView.findViewById(R.id.textviewTen);
            holder.txtDate = (TextView) convertView.findViewById(R.id.textviewDate);
            holder.txtTime = (TextView) convertView.findViewById(R.id.textviewTime);

            holder.imgDelete = (ImageView) convertView.findViewById(R.id.imageviewDelete);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_id);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }



        final CongViec congViec = congViecList.get(position);
        holder.txtTen.setText(congViec.getTenCV());
        holder.txtDate.setText(congViec.getDateCV());
        holder.txtTime.setText(congViec.getTimeCV());

        if (congViec.getCheck() == 1) {
            holder.checkBox.setChecked(true);
            holder.txtTen.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtTime.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else
        {
            holder.checkBox.setChecked(false);
            holder.txtTen.setPaintFlags(holder.txtTen.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtDate.setPaintFlags(holder.txtDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtTime.setPaintFlags(holder.txtTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }


        //bắt sự kiện xóa và sửa
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.DialogSuaCongViec(congViec.getTenCV(), congViec.getDateCV(), congViec.getTimeCV() ,congViec.getIdCV());
            }
        });

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.DialogXoaCV(congViec.getTenCV(), congViec.getIdCV());
            }
        });

        //bắt sự kiện checkbox
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    holder.checkBox.setChecked(true);
                    holder.txtTen.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.txtDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.txtTime.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    congViec.setCheck(1);
                }
                else
                {
                    holder.checkBox.setChecked(false);
                    holder.txtTen.setPaintFlags(holder.txtTen.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.txtDate.setPaintFlags(holder.txtDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.txtTime.setPaintFlags(holder.txtTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    congViec.setCheck(0);
                }

                database.QueryData("UPDATE CongViec SET isCheck ='"+congViec.getCheck()+"' WHERE Id = '"+ congViec.getIdCV() +"'");
                handleCheckBox.onClick();
            }
        });

        return convertView;
    }


}
