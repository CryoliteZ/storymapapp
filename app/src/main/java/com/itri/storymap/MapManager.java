package com.itri.storymap;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.itri.storymap.model.Program;

import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 2016/8/19.
 */
public class MapManager{
    private GoogleMap map;
    public MapManager(GoogleMap gm){
        this.map = gm;
    }


    public boolean initMapFocus(List<Program> ps){
        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (Program p : ps ) {
            builder.include(p.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
        return true;
    }

    public boolean setMapWithFilter(List<Program> programs,List<String> filterTagList, ClusterManager mClusterManager){
        map.clear();
        mClusterManager.clearItems(); // calling for sure - maybe it doenst need to be here
        for(Program p: programs){
            boolean flag = true;
            for(String tag: filterTagList) {
                if (!Arrays.asList(p.tags).contains(tag)) {
                    flag = false;
                    break;
                }
            }
            if(flag){
                mClusterManager.addItem(p);
                Log.d("Adding some item", p.opTitle);
            }
        }
        mClusterManager.cluster();
        return true;
    }


}
