package com.curiousfreaks.greword;

import android.graphics.Bitmap;

/**
 * Created by gasaini on 3/24/2018.
 */

public class imageDefinition {

    private Long id;
    private Bitmap bitmap;
    private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public imageDefinition(Bitmap bitmapImage)
    {
        bitmap=bitmapImage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
