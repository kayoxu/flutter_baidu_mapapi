package com.baidu.bmfmap.map.overlayHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.baidu.bmfmap.BMFMapController;
import com.baidu.bmfmap.R;
import com.baidu.bmfmap.utils.Constants;
import com.baidu.bmfmap.utils.Env;
import com.baidu.bmfmap.utils.converter.FlutterDataConveter;
import com.baidu.bmfmap.utils.converter.TypeConverter;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MarkerHandler extends OverlayHandler {

    private static final String TAG = "MarkerHandler";

    private final HashMap<String, Overlay> mMarkerMap = new HashMap<>();
    private final HashMap<String, BitmapDescriptor> mMarkerBitmapMap = new HashMap<>();

    public MarkerHandler(BMFMapController bmfMapController) {
        super(bmfMapController);
    }

    @Override
    public void handlerMethodCall(MethodCall call, MethodChannel.Result result) {
        if (null == call) {
            result.success(false);
            return;
        }

        String methodId = call.method;
        if (TextUtils.isEmpty(methodId)) {
            result.success(false);
            return;
        }

        boolean ret = false;
        switch (methodId) {
            case Constants.MethodProtocol.MarkerProtocol.sMapAddMarkerMethod:
                ret = addMarker(call);
                break;
            case Constants.MethodProtocol.MarkerProtocol.sMapAddMarkersMethod:
                ret = addMarkers(call);
                break;
            case Constants.MethodProtocol.MarkerProtocol.sMapRemoveMarkerMethod:
                ret = removeMarker(call);
                break;
            case Constants.MethodProtocol.MarkerProtocol.sMapRemoveMarkersMethod:
                ret = removeMarkers(call);
                break;
            case Constants.MethodProtocol.MarkerProtocol.sMapCleanAllMarkersMethod:
                ret = cleanAllMarker(call);
                break;
            case Constants.MethodProtocol.MarkerProtocol.sMapUpdateMarkerMemberMethod:
                ret = updateMarkerMember(call, result);
                break;
            default:
                break;
        }

        result.success(ret);
    }

    @Override
    public void clean() {
        if (mMarkerMap != null && mMarkerMap.size() > 0) {
            Iterator overlayIterator = mMarkerMap.values().iterator();
            Overlay overlay;
            while (overlayIterator.hasNext()) {
                overlay = (Overlay) overlayIterator.next();
                if (null != overlay) {
                    overlay.remove();
                }
            }
            mMarkerMap.clear();
        }

        if (mMarkerBitmapMap != null && mMarkerBitmapMap.size() > 0) {
            Iterator bitmapDescriptorIterator = mMarkerBitmapMap.values().iterator();
            BitmapDescriptor bitmapDescriptor;
            while (bitmapDescriptorIterator.hasNext()) {
                bitmapDescriptor = (BitmapDescriptor) bitmapDescriptorIterator.next();
                if (null != bitmapDescriptor) {
                    bitmapDescriptor.recycle();
                }
            }
            mMarkerBitmapMap.clear();
        }
    }

    private boolean addMarker(MethodCall call) {
        Map<String, Object> argument = call.arguments();
        if (null == argument) {
            return false;
        }

        return addMarkerImp(argument);
    }

    private boolean addMarkerImp(Map<String, Object> argument) {
        if (Env.DEBUG) {
            Log.d(TAG, "addMarkerImp enter");
        }
        if (null == argument) {
            return false;
        }

        if (mMapController == null) {
            return false;
        }

        if (mBaiduMap == null) {
            if (Env.DEBUG) {
                Log.d(TAG, "addOneInfoWindowImp mBaidumap is null");
            }
            return false;
        }

        if (!argument.containsKey("id")
                || !argument.containsKey("position")
                || !argument.containsKey("icon")) {
            return false;
        }

        String id = new TypeConverter<String>().getValue(argument, "id");
        if (TextUtils.isEmpty(id)) {
            return false;
        }

        if (mMarkerMap.containsKey(id)) {
            return false;
        }

        MarkerOptions markerOptions = new MarkerOptions();

        setScreenLockPoint(argument, markerOptions);

        // icon是必须的
        String icon = (String) argument.get("icon");
        if (TextUtils.isEmpty(icon)) {
            return false;
        }

        if (!setMarkerOptions(argument, markerOptions, id, icon)) {
            return false;
        }

        Overlay overlay = mBaiduMap.addOverlay(markerOptions);

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("icon", icon);
        bundle.putString("title", (String) argument.get("title"));
        bundle.putString("identifier", (String) argument.get("identifier"));

        overlay.setExtraInfo(bundle);

        mMarkerMap.put(id, overlay);

        return true;
    }

    private boolean setScreenLockPoint(Map<String, Object> argumentMap,
                                       MarkerOptions markerOptions) {
        if (null == argumentMap || null == markerOptions) {
            return false;
        }

        Boolean isLockedToScreen =
                new TypeConverter<Boolean>().getValue(argumentMap, "isLockedToScreen");
        if (null == isLockedToScreen || false == isLockedToScreen) {
            return false;
        }

        Map<String, Object> screenPointToLockMap =
                new TypeConverter<Map<String, Object>>().getValue(argumentMap, "screenPointToLock");
        if (null == screenPointToLockMap
                || !screenPointToLockMap.containsKey("x")
                || !screenPointToLockMap.containsKey("y")) {
            return false;
        }

        Double x = new TypeConverter<Double>().getValue(screenPointToLockMap, "x");
        Double y = new TypeConverter<Double>().getValue(screenPointToLockMap, "y");
        if (null == x || null == y) {
            return false;
        }

        Point point = new Point(x.intValue(), y.intValue());

        markerOptions.fixedScreenPosition(point);
        return true;
    }


    //把布局变成Bitmap
    public static Bitmap getViewBitmap(View addViewContent) {

        addViewContent.setDrawingCacheEnabled(true);

        addViewContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0,
                addViewContent.getMeasuredWidth(),
                addViewContent.getMeasuredHeight());

        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        return bitmap;
    }

    /**
     * 解析并设置markertions里的信息
     *
     * @return
     */
    private boolean setMarkerOptions(Map<String, Object> markerOptionsMap,
                                     MarkerOptions markerOptions, String id, String icon) {

        Map<String, Object> latlngMap = (Map<String, Object>) markerOptionsMap.get("position");

        LatLng latLng = FlutterDataConveter.mapToLatlng(latlngMap);
        if (null == latLng) {
            if (Env.DEBUG) {
                Log.d(TAG, "latLng is null");
            }
            return false;
        }
        markerOptions.position(latLng);

        BitmapDescriptor bitmapDescriptor2 = null;
        BitmapDescriptor bitmapDescriptor3 = null;
        BitmapDescriptor bitmapDescriptor =
                BitmapDescriptorFactory.fromAsset("flutter_assets/" + icon);
        if (null == bitmapDescriptor) {
            return false;
        }

        boolean titleHorizontal = false;
        Object horizontal = markerOptionsMap.get("titleHorizontal");
        if (markerOptionsMap.containsKey("titleHorizontal") && (horizontal instanceof Boolean)) {
            titleHorizontal = (boolean) markerOptionsMap.get("titleHorizontal");
        }

        boolean hasIcon2 = false;
        boolean hasIcon3 = false;


        if (markerOptionsMap.containsKey("title") && markerOptionsMap.get("title") != null && markerOptionsMap.get("title") != null) {

            if (markerOptionsMap.get("title") != "")
                bitmapDescriptor = getBitmapDescriptor(markerOptionsMap, bitmapDescriptor, titleHorizontal);

            if (null == bitmapDescriptor) return false;

            if (markerOptionsMap.get("icon2") != null) {
                String icon2 = (String) markerOptionsMap.get("icon2");
                bitmapDescriptor2 =
                        BitmapDescriptorFactory.fromAsset("flutter_assets/" + icon2);
                if (null != bitmapDescriptor2) {
                    hasIcon2 = true;
                    if (markerOptionsMap.get("title") != "")
                        bitmapDescriptor2 = getBitmapDescriptor(markerOptionsMap, bitmapDescriptor2, titleHorizontal);
                }
            }

            if (markerOptionsMap.get("icon3") != null) {
                String icon3 = (String) markerOptionsMap.get("icon3");
                bitmapDescriptor3 =
                        BitmapDescriptorFactory.fromAsset("flutter_assets/" + icon3);
                if (null != bitmapDescriptor3) {
                    hasIcon3 = true;
                    if (markerOptionsMap.get("title") != "")
                        bitmapDescriptor3 = getBitmapDescriptor(markerOptionsMap, bitmapDescriptor3, titleHorizontal);
                }
            }
        }

        if (!hasIcon2 && !hasIcon3) {
            markerOptions.icon(bitmapDescriptor);
            mMarkerBitmapMap.put(id, bitmapDescriptor);
            if (titleHorizontal) {
                markerOptions.anchor(0.0f, 0);
            } else {
                markerOptions.anchor(0.5f, 0);
            }
        } else {
            ArrayList<BitmapDescriptor> bs = new ArrayList<>();
            bs.add(bitmapDescriptor);

            if (null != bitmapDescriptor2) {
                bs.add(bitmapDescriptor2);
                mMarkerBitmapMap.put(id + 1000000, bitmapDescriptor2);
            }
            if (null != bitmapDescriptor3) {
                bs.add(bitmapDescriptor3);
                mMarkerBitmapMap.put(id + 1000001, bitmapDescriptor3);
            }
            markerOptions.icons(bs);
            if (titleHorizontal) {
                markerOptions.anchor(0.0f, 0.5f);
            } else {
                markerOptions.anchor(0.5f, 0);
            }
        }
        //centerOffset
        Map<String, Object> centerOffset =
                new TypeConverter<Map<String, Object>>().getValue(markerOptionsMap, "centerOffset");
        if (null != centerOffset) {
            Double y = new TypeConverter<Double>().getValue(centerOffset, "y");
            if (null != y) {
                markerOptions.yOffset(y.intValue());
            }
        }

        Boolean enable = new TypeConverter<Boolean>().getValue(markerOptionsMap, "enabled");
        if (markerOptionsMap.containsKey("enabled")) {
            if (Env.DEBUG) {
                Log.d(TAG, "enbale" + enable);
            }
            markerOptions.clickable(enable);
        }

        Boolean draggable = new TypeConverter<Boolean>().getValue(markerOptionsMap, "draggable");
        if (null != draggable) {
            markerOptions.draggable(draggable);
        }

        Integer zIndex = new TypeConverter<Integer>().getValue(markerOptionsMap, "zIndex");
        if (null != zIndex) {
            markerOptions.zIndex(zIndex);
        }

        Boolean visible = new TypeConverter<Boolean>().getValue(markerOptionsMap, "visible");
        if (null != visible) {
            markerOptions.visible(visible);
        }

        Double scaleX = new TypeConverter<Double>().getValue(markerOptionsMap, "scaleX");
        if (null != scaleX) {
            markerOptions.scaleX(scaleX.floatValue());
        }

        Double scaleY = new TypeConverter<Double>().getValue(markerOptionsMap, "scaleY");
        if (null != scaleY) {
            markerOptions.scaleY(scaleY.floatValue());
        }

        Double alpha = new TypeConverter<Double>().getValue(markerOptionsMap, "alpha");
        if (null != alpha) {
            markerOptions.alpha(alpha.floatValue());
        }

        Boolean isPerspective =
                new TypeConverter<Boolean>().getValue(markerOptionsMap, "isPerspective");
        if (null != isPerspective) {
            markerOptions.perspective(isPerspective);
        }

        return true;
    }

    @Nullable
    private BitmapDescriptor getBitmapDescriptor(Map<String, Object> markerOptionsMap, BitmapDescriptor bitmapDescriptor, boolean titleHorizontal) {
        LayoutInflater inflater = LayoutInflater.from(mMapController.getContext());
        View root = inflater.inflate(titleHorizontal ? R.layout.map_dot_info2 : R.layout.map_dot_info, null);
        if (markerOptionsMap.get("title2") != null) {
            root = inflater.inflate(titleHorizontal ? R.layout.map_dot_info_more2 : R.layout.map_dot_info_more, null);
            TextView tv2 = root.findViewById(R.id.title2);
            tv2.setText(markerOptionsMap.get("title2") + "");
            if (markerOptionsMap.containsKey("title2Color") && markerOptionsMap.get("title2Color") != null) {
                tv2.setTextColor(Color.parseColor("#" + (String) markerOptionsMap.get("title2Color")));
            }
        }

        TextView tv = root.findViewById(R.id.title);
        ImageView iv = root.findViewById(R.id.icon);
        tv.setText(markerOptionsMap.get("title") + "");
        iv.setImageBitmap(bitmapDescriptor.getBitmap());
        if (markerOptionsMap.containsKey("titleColor") && markerOptionsMap.get("titleColor") != null) {
            tv.setTextColor(Color.parseColor("#" + (String) markerOptionsMap.get("titleColor")));
        }

        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getViewBitmap(root));
        return bitmapDescriptor;
    }

    private boolean addMarkers(MethodCall call) {

        if (Env.DEBUG) {
            Log.d(TAG, "addMarkers enter");
        }
        if (null == call) {
            return false;
        }

        List<Object> arguments = call.arguments();
        if (null == arguments) {
            return false;
        }

        Iterator itr = arguments.iterator();
        while (itr.hasNext()) {
            Map<String, Object> argument = (Map<String, Object>) itr.next();
            addMarkerImp(argument);

        }
        return true;
    }

    private boolean removeMarker(MethodCall call) {
        Map<String, Object> argument = call.arguments();
        if (null == argument) {
            return false;
        }

        removeMarkerImp(argument);
        return true;
    }

    private boolean removeMarkerImp(Map<String, Object> argument) {
        if (mMarkerMap == null) {
            return false;
        }
        String id = new TypeConverter<String>().getValue(argument, "id");
        Overlay overlay = mMarkerMap.get(id);
        BitmapDescriptor bitmapDescriptor = mMarkerBitmapMap.get(id);
        BitmapDescriptor bitmapDescriptor2 = mMarkerBitmapMap.get(id + 1000000);
        BitmapDescriptor bitmapDescriptor3 = mMarkerBitmapMap.get(id + 1000001);

        boolean ret = true;
        if (null != overlay) {
            overlay.remove();
            mMarkerMap.remove(id);
        } else {
            ret = false;
        }

        if (null != bitmapDescriptor2) {
            bitmapDescriptor2.recycle();
            mMarkerBitmapMap.remove(id + 1000000);
        }
        if (null != bitmapDescriptor3) {
            bitmapDescriptor3.recycle();
            mMarkerBitmapMap.remove(id + 1000001);
        }
        if (null != bitmapDescriptor) {
            bitmapDescriptor.recycle();
            mMarkerBitmapMap.remove(id);
        } else {
            ret = false;
        }

        return ret;
    }

    private boolean removeMarkers(MethodCall call) {
        List<Object> markersList = call.arguments();
        if (null == markersList) {
            return false;
        }

        Iterator itr = markersList.iterator();
        while (itr.hasNext()) {
            Map<String, Object> marker = (Map<String, Object>) itr.next();
            if (null != marker) {
                removeMarkerImp(marker);
            }

        }

        return true;
    }

    private boolean cleanAllMarker(MethodCall call) {
        clean();
        return true;
    }

    /**
     * 更新marker属性
     *
     * @param call
     * @param result
     * @return
     */
    private boolean updateMarkerMember(MethodCall call, MethodChannel.Result result) {
        if (mMarkerMap == null) {
            return false;
        }
        Map<String, Object> argument = call.arguments();
        if (null == argument) {
            return false;
        }

        String id = new TypeConverter<String>().getValue(argument, "id");
        if (TextUtils.isEmpty(id)) {
            return false;
        }

        if (!mMarkerBitmapMap.containsKey(id)) {
            return false;
        }

        Marker marker = (Marker) mMarkerMap.get(id);
        if (null == marker) {
            return false;
        }

        String member = new TypeConverter<String>().getValue(argument, "member");
        if (TextUtils.isEmpty(member)) {
            return false;
        }

        Object value = argument.get("value");
        if (null == value) {
            return false;
        }

        boolean ret = false;
        switch (member) {
            case "title":
                String title = (String) value;
                if (!TextUtils.isEmpty(title)) {
                    marker.setTitle(title);
                    ret = true;
                }
                break;
            case "position":
                Map<String, Object> position = (Map<String, Object>) value;
                LatLng latLng = FlutterDataConveter.mapToLatlng(position);
                if (null != latLng) {
                    marker.setPosition(latLng);
                    ret = true;
                }
                break;
            case "isLockedToScreen":
                Boolean isLockedToScreen = (Boolean) value;
                if (null != isLockedToScreen && isLockedToScreen) {

                    Map<String, Object> pointMap =
                            new TypeConverter<Map<String, Object>>().getValue(argument,
                                    "screenPointToLock");

                    Point point = FlutterDataConveter.mapToPoint(pointMap);
                    if (null != point) {
                        marker.setFixedScreenPosition(point);
                        ret = true;
                    }
                }
                break;
            case "icon":
                String icon = (String) value;
                BitmapDescriptor bitmapDescriptor = mMarkerBitmapMap.get(id);

                if (null != bitmapDescriptor) {
                    bitmapDescriptor.recycle();
                    mMarkerBitmapMap.remove(id);
                }

                bitmapDescriptor = BitmapDescriptorFactory.fromAsset("flutter_assets/" + icon);

                if (null != bitmapDescriptor) {
                    marker.setIcon(bitmapDescriptor);
                    mMarkerBitmapMap.put(id, bitmapDescriptor);
                    ret = true;
                }
                break;
            case "centerOffset":
                Map<String, Object> centerOffset = (Map<String, Object>) value;
                if (null != centerOffset) {
                    Double y = new TypeConverter<Double>().getValue(centerOffset, "y");
                    if (null != y) {
                        marker.setYOffset(y.intValue());
                        ret = true;
                    }
                }
                break;
            case "enabled":
                Boolean enabled = (Boolean) value;
                if (null != enabled) {
                    marker.setClickable(enabled);
                    ret = true;
                }
                break;
            case "draggable":
                Boolean draggable = (Boolean) value;
                if (null != draggable) {
                    marker.setDraggable(draggable);
                    ret = true;
                }
                break;
            case "visible":
                Boolean visible = (Boolean) value;
                if (null != visible) {
                    marker.setVisible(visible);
                    ret = true;
                }
                break;
            case "zIndex":
                Integer zIndex = (Integer) value;
                if (null != zIndex) {
                    marker.setZIndex(zIndex);
                    ret = true;
                }
                break;
            case "scaleX":
                Double scaleX = (Double) value;
                if (null != scaleX) {
                    marker.setScaleX(scaleX.floatValue());
                    ret = true;
                }
                break;
            case "scaleY":
                Double scaleY = (Double) value;
                if (null != scaleY) {
                    marker.setScaleY(scaleY.floatValue());
                    ret = true;
                }
                break;
            case "alpha":
                Double alpha = (Double) value;
                if (null != alpha) {
                    marker.setAlpha(alpha.floatValue());
                    ret = true;
                }
                break;
            case "isPerspective":
                Boolean isPerspective = (Boolean) value;
                if (null != isPerspective) {
                    marker.setPerspective(isPerspective);
                    ret = true;
                }
                break;
            default:
                break;
        }

        return ret;
    }
}
