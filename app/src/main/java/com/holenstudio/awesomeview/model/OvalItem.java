package com.holenstudio.awesomeview.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Holen on 2016/6/14.
 */
public class OvalItem {
    private int mIconSrc;
    private List<OvalItem> mSubItems;

    public OvalItem(int mIconSrc) {
        this(mIconSrc, null);
    }

    public OvalItem(int mIconSrc, List<OvalItem> mSubItems) {
        this.mIconSrc = mIconSrc;
        this.mSubItems = mSubItems;
    }

    public int getIconSrc() {
        return mIconSrc;
    }

    public void setIconSrc(int iconSrc) {
        this.mIconSrc = iconSrc;
    }

    public List<OvalItem> getSubItems() {
        return mSubItems;
    }

    public void setSubItems(List<OvalItem> subItems) {
        this.mSubItems = subItems;
    }

    public void addSubItems(OvalItem item) {
        if (mSubItems == null) {
            mSubItems = new ArrayList<>();
        }
        mSubItems.add(item);
    }

    @Override
    public String toString() {
        return "OvalItem{" +
                "iconSrc=" + mIconSrc +
                ", subItems=" + mSubItems +
                '}';
    }
}
