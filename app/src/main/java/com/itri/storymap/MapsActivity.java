package com.itri.storymap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final LatLng SYDNEY = new LatLng(-33.88,151.21);
    private static final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        setUpMap();
//        Intent myIntent = new Intent(this, StoryMapClusterActivity.class);
//        startActivity(myIntent);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mMap != null) {
            return;
        }
        mMap = map;
//        LoadJSONManager getMapData = new LoadJSONManager();
//        String TEST_DATA = getMapData.loadJSONFromAsset(this.getApplicationContext());
//        Log.wtf("json", TEST_DATA);
        MapDataManager mMapDataManager = new MapDataManager();

        final LatLng[] randPos = new LatLng[100];
        for(int i = 0; i < 100; ++i) {
            randPos[i] = new LatLng(24 + Math.random(), 121 + Math.random());
            mMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cicon))
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(randPos[i]));
        }
        for(int i = 0; i < 0; ++i){
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Move the camera instantly to Sydney with a zoom of 15.
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(randPos[(int)(Math.random()*99)], 15));

                }
            }, 2000*i);
        }


        mMap.setMyLocationEnabled(true);
    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    protected GoogleMap getMap() {
        return mMap;
    }
}
