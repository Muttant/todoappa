package com.example.todolist;

public class CongViec {
    private int IdCV;
    private String TenCV;
    private String DateCV;
    private String TimeCV;
    private int isCheck;



    public CongViec(int idCV, String tenCV, String dateCV, String timeCV, int isCheck)
    {
        IdCV = idCV;
        TenCV = tenCV;
        DateCV = dateCV;
        TimeCV = timeCV;
        this.isCheck = isCheck;
    }
    public CongViec(int idCV, String dateCV, String timeCV, int isCheck)
    {
        IdCV = idCV;
        DateCV = dateCV;
        TimeCV = timeCV;
        this.isCheck = isCheck;
    }

    public int getIdCV() {
        return IdCV;
    }

    public String getTenCV() {
        return TenCV;
    }

    public void setIdCV(int idCV) {
        IdCV = idCV;
    }

    public void setTenCV(String tenCV) {
        TenCV = tenCV;
    }
    public String getDateCV() {
        return DateCV;
    }

    public void setDateCV(String dateCV) {
        DateCV = dateCV;
    }

    public String getTimeCV() {
        return TimeCV;
    }

    public void setTimeCV(String timeCV) {
        TimeCV = timeCV;
    }

    public int getCheck() {
        return isCheck;
    }

    public void setCheck(int check) {
        isCheck = check;
    }
}
