package com.example.a17280.whether.entity;



/**
 * Created by 17280 on 2019/5/5.
 *
 */

public class Whether {
    private int imageView_whether;
    private String string_whether;
    private String string_day;
    private String string_tempareture;


    public void setImageView_whether(int imageView_whether) {
        this.imageView_whether = imageView_whether;
    }
    public void setString_whether(String string_whether) {
        this.string_whether = string_whether;
    }
    public void setString_day(String string_day) {
        string_day = string_day.substring(5);
        this.string_day = string_day;
    }
    public void setString_tempareture(String string_tempareture) {
        this.string_tempareture = string_tempareture;
    }

    public int getImageView_whether() {
        return imageView_whether;
    }
    public String getString_whether() {
        return string_whether;
    }
    public String getString_day() {
        return string_day;
    }
    public String getString_tempareture() {
        return string_tempareture;
    }
}
