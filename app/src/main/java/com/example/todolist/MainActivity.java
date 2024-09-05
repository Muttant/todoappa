package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements IHandleCheckBox {

    public static final DateTimeFormatter DD_MM_YY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter YY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Database database;
    ListView lvCongViec;
    ImageView imgAdd;
    List<CongViec> arrayCongViec;
    CongViecAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgAdd = (ImageView) findViewById(R.id.img_add);
        lvCongViec = (ListView) findViewById(R.id.listviewCongViec);
        arrayCongViec = new ArrayList<>();

        //khởi tạo database GhiChu
        database = new Database(this, "ghichu.sqlite", null, 2);

        adapter = new CongViecAdapter(this, R.layout.item_cong_viec, arrayCongViec, database, this);
        lvCongViec.setAdapter(adapter);

//        database.QueryData("DROP TABLE IF EXISTS CongViec;");

        //tạo bảng CongViec
        database.QueryData("CREATE TABLE IF NOT EXISTS CongViec(Id INTEGER PRIMARY KEY AUTOINCREMENT, TenCV VARCHAR(200), DateCV VARCHAR(200), TimeCV VARCHAR(200), isCheck INTEGER); ");
       // Lấy dữ liệu tại bảng CongViec
        GetDataCongViec();

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogThem();
            }
        });
    }

    //dialog sửa ghi chú
    public void DialogSuaCongViec(String ten, String date, String time, final int id) {
        final  Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sua);

        final EditText edtTenCV = (EditText) dialog.findViewById(R.id.editTextTenCVEdit);
        final TextView tvDate = (TextView) dialog.findViewById(R.id.textDateCVEdit);
        final TextView tvTime = (TextView) dialog.findViewById(R.id.textTimeCVEdit);

        Button btnXacNhan = (Button) dialog.findViewById(R.id.buttonXacNhan);
        Button btnHuy = (Button) dialog.findViewById(R.id.buttonHuyEdit);

        edtTenCV.setText(ten);
        tvDate.setText(date);
        tvTime.setText(time);

        //bắt sự kiện chọn giờ trong dialog
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvDate.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Chưa nhập ngày",Toast.LENGTH_SHORT).show();
                    return;
                }
                ChonGio(tvDate.getText().toString(), tvTime, edtTenCV);
            }
        });
        //bắt sự kiện chọn ngày trong dialog
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChonNgay(tvDate);
            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenMoi = edtTenCV.getText().toString().trim();
                String dateMoi = tvDate.getText().toString().trim();
                if (!dateMoi.isEmpty()) {
                    dateMoi = parseFormat(dateMoi, DD_MM_YY, YY_MM_DD);
                }
                String timeMoi = tvTime.getText().toString().trim();

                database.QueryData("UPDATE CongViec SET TenCV ='"+tenMoi+"', DateCV = '"+dateMoi+"', TimeCV = '"+timeMoi+"' WHERE Id = '"+ id +"'");
                Toast.makeText(MainActivity.this, "Đã cập nhập",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                GetDataCongViec();
            }
        });
        dialog.show();
    }

    //dialog xác nhận xóa ghi chú
    public void DialogXoaCV(String tencv, final int id){
        AlertDialog.Builder dialogXoa = new AlertDialog.Builder(this);
        dialogXoa.setMessage("Bạn có muốn xóa không?");
        dialogXoa.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.QueryData("DELETE FROM CongViec WHERE Id = '"+id+"'");
                Toast.makeText(MainActivity.this, "Đã xóa",Toast.LENGTH_SHORT).show();
                GetDataCongViec();
            }
        });
        dialogXoa.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogXoa.show();
    }

    //lấy dữ liệu trong bảng CongViec
    private void GetDataCongViec(){
        this.updateListView("SELECT * FROM CongViec order by isCheck asc, id desc");
    }

    // select and update data listView
    private void updateListView(@NonNull String sql){
        Cursor dataCongViec = database.GetData(sql);
        arrayCongViec.clear();
        while (dataCongViec.moveToNext()){
            int id = dataCongViec.getInt(0);
            String ten = dataCongViec.getString(1);
            String ngay = dataCongViec.getString(2);
            if (!ngay.isEmpty()) {
                ngay = parseFormat(ngay, YY_MM_DD, DD_MM_YY);
            }
            String gio = dataCongViec.getString(3);
            int isCheck = dataCongViec.getInt(4);
            arrayCongViec.add(new CongViec(id, ten, ngay, gio, isCheck));
        }
        adapter.notifyDataSetChanged();
    }

    //chọn sự kiện Thêm ghi chú trên thanh menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menusearch, menu);
        getMenuInflater().inflate(R.menu.menu_setting, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search here");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(MainActivity.this, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(MainActivity.this, "onQueryTextChange", Toast.LENGTH_SHORT).show();
                updateListView("SELECT * FROM CongViec WHERE tencv like '%"+newText+"%' order by isCheck asc, id desc");
                return false;
            }
        });

//        return super.onCreateOptionsMenu(menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.tt_darkMode:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
                break;
            case R.id.tt_lightMode:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
                break;
            case R.id.tt_all:
                this.updateListView("SELECT * FROM CongViec order by isCheck asc, id desc");
                break;
            case R.id.tt_coming:
                LocalDate date = LocalDate.now();
                this.updateListView("SELECT * FROM CongViec WHERE (isCheck = 0 or isCheck is null) and (datecv = '' or(datecv >= '"+date.format(YY_MM_DD)+"' and datecv <= '"+date.plusDays(1).format(YY_MM_DD)+"')) order by id desc");
                break;
            case R.id.tt_done:
                this.updateListView("SELECT * FROM CongViec WHERE isCheck = 1 order by id desc");
                break;
            default: break;
        }
//        if(item.getItemId() == R.id.menuSetting){
//            startActivity(new Intent(MainActivity.this, SettingActivity.class));
//        }
        return true;
    }

    // thêm ghi chú
    private void DialogThem(){
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_cong_viec);

        final EditText edtTen = (EditText) dialog.findViewById(R.id.editTextTenCV);
        final TextView txtDate = (TextView) dialog.findViewById(R.id.textDateCV);
        final TextView txtTime = (TextView) dialog.findViewById(R.id.textTimeCV);

        Button btnThem = (Button) dialog.findViewById(R.id.buttonThem);
        Button btnHuy =  (Button) dialog.findViewById(R.id.buttonHuy);

        //bắt sự kiện chọn giờ
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChonGio(txtDate.getText().toString(), txtTime, edtTen);
            }
        });
        //bắt sự kiện chọn ngày
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChonNgay(txtDate);
            }
        });
        //bắt sự kiện xác nhận thêm ghi chú
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tencv = edtTen.getText().toString();
                String datecv = txtDate.getText().toString();
                if (!datecv.isEmpty()) {
                    datecv = parseFormat(datecv, DD_MM_YY, YY_MM_DD);
                }
                String timecv = txtTime.getText().toString();

                //kiểm tra không nhập gì vào ô editText
                if(tencv.equals("")){
                    Toast.makeText(MainActivity.this, "Vui lòng nhập ghi chú công việc.",Toast.LENGTH_SHORT).show();
                }else {
                    //insert database
                    database.QueryData("INSERT INTO CongViec (Id, TenCV, DateCV, TimeCV, isCheck) VALUES (null, '" + tencv + "','"+datecv+"','"+timecv+"', 0);");
                    Toast.makeText(MainActivity.this, "Đã thêm.",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    GetDataCongViec();
                }
            }
        });
        //bắt sự kiện hủy
        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //dialog chọn giờ
    private void ChonGio(String ngay, final TextView txtTime, final EditText edtTen) {
        final Calendar calendar = Calendar.getInstance();

        int gio = calendar.get(Calendar.HOUR_OF_DAY);
        int phut = calendar.get(Calendar.MINUTE);
        final TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                LocalDate date;
                if (ngay.isEmpty()) {
                    date = LocalDate.now();
                } else {
                    date = LocalDate.parse(ngay, DD_MM_YY);
                }
                calendar.set(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),hourOfDay, minute);
                txtTime.setText(simpleDateFormat.format(calendar.getTime()));
                setMultipleAlarms(calendar, edtTen);
            }
        }, gio, phut, true);
        timePickerDialog.show();

    }

    //dialog chọn ngày
    private void ChonNgay(final TextView txtDate) {
        final Calendar calendar = Calendar.getInstance();
        int ngay = calendar.get(Calendar.DATE);
        int thang = calendar.get(Calendar.MONTH);
        int nam = calendar.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                calendar.set(year,month,dayOfMonth);
                txtDate.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, nam, thang, ngay);
        datePickerDialog.show();
    }

    //đặt báo thức
    public void setMultipleAlarms(Calendar calendar, EditText edtTen){

        //đặt chuông báo
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT
        );
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        //Tạo thông báo hiển thị trên màn hình
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int notification = NotificationManager.IMPORTANCE_MAX;
            CharSequence name = "To Do List";
            NotificationChannel channel = new NotificationChannel("TDL_1", name, notification);
            channel.setDescription(edtTen.getText().toString());
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        addNotification(edtTen.getText().toString());

    }

    private void addNotification(String des){
        String strTitle = "To Do List";
        String strMsg = des;
        Intent notificationIntent = new Intent(this, NotificationDetailActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("message", strMsg);
        notificationIntent.putExtra("title", strTitle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"TDL_1")
                .setSmallIcon(R.drawable.icon1)
                .setContentTitle(strTitle)
                .setContentText(strMsg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.BLUE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(R.mipmap.ic_launcher,"DỪNG", pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }


    @Override
    public void onClick() {
        this.GetDataCongViec();
    }

    private static String parseFormat(String input, DateTimeFormatter inputFormatter, DateTimeFormatter outputFormatter) {
        return LocalDate.parse(input, inputFormatter).format(outputFormatter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.menusearch, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setQueryHint("Search here");
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//                return false;
//            }
//        });
//
//        return super.onCreateOptionsMenu(menu);
//    }
}
