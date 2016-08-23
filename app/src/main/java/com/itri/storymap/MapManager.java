package com.itri.storymap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.itri.storymap.model.Program;

import java.util.List;

/**
 * Created by user on 2016/8/19.
 */
public class MapManager{
    private GoogleMap map;
    public MapManager(GoogleMap gm){
        this.map = gm;
    }


    public  boolean initMapFocus(List<Program> ps){
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
}
