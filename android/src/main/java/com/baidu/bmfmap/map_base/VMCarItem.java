package com.baidu.bmfmap.map_base;

import android.os.Bundle;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.bmfmap.map_base.clusterutil.clustering.ClusterItem;

import java.util.Map;


/**
 * TFBlueAndroidXNew
 * null.java
 * <p>
 * Created by kayoxu on 3/12/21 at 10:20 AM
 * Copyright © 2021 kayoxu. All rights reserved.
 */

public class VMCarItem implements ClusterItem {
    private LatLng mPosition;
    private int status;
    private Bundle bundle;
    private int rotate;
    private int zIndex = 9;
    private Map<String, Object> argument;

    public VMCarItem(LatLng position, int status, int rotate, Bundle bundle) {
        mPosition = position;
        this.status = status;
        this.bundle = bundle;
        this.rotate = rotate;
    }

    public Map<String, Object> getArgument() {
        return argument;
    }

    public void setArgument(Map<String, Object> argument) {
        this.argument = argument;
    }

    public VMCarItem() {
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public void setmPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public int getRotate() {
        return rotate + -90;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public Bundle getExtraInfo() {
        return bundle;
    }

//    @Override
//    public BitmapDescriptor getBitmapDescriptor() {
//        int drawableRes = icon;
//
//        return BitmapDescriptorFactory
//                .fromResource(drawableRes);
//    }
}