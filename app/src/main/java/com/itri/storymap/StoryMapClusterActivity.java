package com.itri.storymap;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.itri.storymap.model.Program;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.SweetSheet;
import com.mingle.sweetpick.ViewPagerDelegate;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 2016/8/9.
 */
public class StoryMapClusterActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Program>, ClusterManager.OnClusterInfoWindowClickListener<Program>, ClusterManager.OnClusterItemClickListener<Program>, ClusterManager.OnClusterItemInfoWindowClickListener<Program> {
    private ClusterManager<Program> mClusterManager;
    private Random mRandom = new Random(1984);
    public Drawable mDrawable;
    public MapDataManager mMapDataManager = new MapDataManager();
    public MapManager mMapManager;
    private SweetSheet mSweetSheet;
    private FrameLayout fl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fl = (FrameLayout)findViewById(R.id.fl);
        setupViewpager();
        initFilterButtonListener();
        initHomeButtonListener();




    }

    /**
     * Draws program photos inside markers (using IconGenerator).
     * When there are multiple programs in the cluster, draw multiple photos (using MultiDrawable).
     */

    private class ProgramRenderer extends DefaultClusterRenderer<Program> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;


        public ProgramRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);

        }

        @Override
        protected void onBeforeClusterItemRendered(Program program, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.

            mImageView.setImageResource(R.drawable.cicon);
            Picasso.with(getApplicationContext()).load(program.iconURL).into(mImageView);
            Log.d("Picasso load finish", program.opID);



            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(program.opTitle);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Program> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;
            Drawable  drawable = null;
            for (Program p : cluster.getItems()) {
//                 Draw 4 at most.
               drawable = getResources().getDrawable(p.profilePhoto);
                Random rand = new Random();
                int rdn = rand.nextInt(4) + 1;
                if (profilePhotos.size() == 1) break;

                NetworkOperationAsync task = new NetworkOperationAsync();
                try{
                    drawable = mMapDataManager.loadDrawable.get(p.opID);
                    Log.d("isloadornot", Boolean.toString(drawable != null));
                    if(drawable == null){
                        drawable = task.execute(p.iconURL).get();
                        mMapDataManager.loadDrawable.put(p.opID, drawable);
                    }
                    drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);


                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(profilePhotos.size() <= 0) return;

            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }




        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    private class NetworkOperationAsync extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... urls) {
            Drawable d = null;
            try {
                InputStream input = (new URL(urls[0])).openStream();
                d = Drawable.createFromStream(input, null);
                input.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return  d;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            mDrawable = result;
        }
    }

    public  Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(this.getApplicationContext().getResources(), x);
    }

    @Override
    public boolean onClusterClick(Cluster<Program> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().opID;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Program> cluster) {
        // Does nothing, but you could go to a list of the programs.
    }

    @Override
    public boolean onClusterItemClick(Program item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Program item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }




    protected void startDemo() {
        mMapManager = new MapManager(getMap());
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24, 121), 9.5f));
        mClusterManager = new ClusterManager<Program>(this, getMap());
        mClusterManager.setRenderer(new ProgramRenderer());
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        getMap().setOnCameraIdleListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
//
        addItems();
        mClusterManager.cluster();
    }

    private void addItems(){
        if(mMapDataManager.initLoadJSONFromAsset(this.getApplicationContext())){
            List <Program> ps = mMapDataManager.getProgramListItem();
            mClusterManager.addItems(ps);
        }

    }

    private void initFilterButtonListener(){
        FloatingActionButton filterBtn = (FloatingActionButton)findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                }
                else
                    mSweetSheet.toggle();
            }
        });
    }

    private void initHomeButtonListener(){
        FloatingActionButton homeBtn = (FloatingActionButton)findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapManager.initMapFocus(mMapDataManager.programs);
            }
        });
    }

    private void setupViewpager() {
        final ArrayList<MenuEntity> list = new ArrayList<>();
        for(int i = 0; i < 20; ++i){
            MenuEntity menuEntity = new MenuEntity();
            menuEntity.titleColor = 0xffb3b3b3;
            menuEntity.title = "title " + String.valueOf(i);
            list.add(menuEntity);
        }
        // attach to FrameLayout
        mSweetSheet = new SweetSheet(fl);
        // set list to sweetsheet
        mSweetSheet.setMenuList(list);

        // get window height
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        // set the viewpager
        mSweetSheet.setDelegate(new ViewPagerDelegate(3, (size.y)/2));
        // set background effect (dim)
        mSweetSheet.setBackgroundEffect(new DimEffect(0.87f));
        // set onclickListener
        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity) {
                menuEntity.titleColor = (menuEntity.isChosen)? 0xffb3b3b3:0xff303030;
                menuEntity.isChosen = !menuEntity.isChosen;
                // Reset list (Bad practice, but it works)
                mSweetSheet.setMenuList(list);
                // Not sure if this lin of code works
                ((ViewPagerDelegate)mSweetSheet.getDelegate()).notifyDataSetChanged();
                // Toast.makeText(BaseDemoActivity.this, menuEntity.title + "  " + position, Toast.LENGTH_SHORT).show();
                // If return true, sweetsheet closes, return false does not
                return false;
            }
        });
    }



}
