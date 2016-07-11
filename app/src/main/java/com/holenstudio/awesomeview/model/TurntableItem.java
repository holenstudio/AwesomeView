package com.holenstudio.awesomeview.model;

import android.graphics.Bitmap;

/**
 * Created by hhn6205 on 2016/7/11.
 */
public class TurntableItem {
    Bitmap normalBmp;
    Bitmap selectedBmp;
    Bitmap disabledBmp;
    int index;
    boolean frontDisable;
    boolean backDisable;

    public TurntableItem() {
    }

    public TurntableItem(Bitmap normalBmp, Bitmap selectedBmp, Bitmap disabledBmp, int index, boolean frontDisable, boolean backDisable) {
        this.normalBmp = normalBmp;
        this.selectedBmp = selectedBmp;
        this.disabledBmp = disabledBmp;
        this.index = index;
        this.frontDisable = frontDisable;
        this.backDisable = backDisable;
    }

    public Bitmap getNormalBmp() {
        return normalBmp;
    }

    public void setNormalBmp(Bitmap normalBmp) {
        this.normalBmp = normalBmp;
    }

    public Bitmap getSelectedBmp() {
        return selectedBmp;
    }

    public void setSelectedBmp(Bitmap selectedBmp) {
        this.selectedBmp = selectedBmp;
    }

    public Bitmap getDisabledBmp() {
        return disabledBmp;
    }

    public void setDisabledBmp(Bitmap disabledBmp) {
        this.disabledBmp = disabledBmp;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isFrontDisable() {
        return frontDisable;
    }

    public void setFrontDisable(boolean frontDisable) {
        this.frontDisable = frontDisable;
    }

    public boolean isBackDisable() {
        return backDisable;
    }

    public void setBackDisable(boolean backDisable) {
        this.backDisable = backDisable;
    }

    @Override
    public String toString() {
        return "TurntableItem{" +
                "index=" + index +
                ", frontDisable=" + frontDisable +
                ", backDisable=" + backDisable +
                '}';
    }
}
