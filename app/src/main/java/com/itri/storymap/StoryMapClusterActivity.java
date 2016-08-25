package com.itri.storymap;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.mingle.sweetpick.ViewPagerDelegate;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 2016/8/9.
 */
public class StoryMapClusterActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Program>, ClusterManager.OnClusterInfoWindowClickListener<Program>, ClusterManager.OnClusterItemClickListener<Program>, ClusterManager.OnClusterItemInfoWindowClickListener<Program> {
    private ClusterManager<Program> mClusterManager;
    public Drawable mDrawable;
    public MapDataManager mMapDataManager = new MapDataManager();
    public MapManager mMapManager;
    private SweetSheet tagsSweetSheet,programSweetSheet;
    private FrameLayout fl;
    private final int OCCUR_ZOOM_LEVEL = 21;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);






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

//            mImageView.setImageResource(R.drawable.cicon);
            Picasso.with(getApplicationContext())
                    .load(program.iconURL)
                    .resize(52, 52)
                    .placeholder(R.drawable.cicon)
                    .centerCrop()
                    .into(mImageView);





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
            //   Draw 4 at most.
                Random rand = new Random();
                int rdn = rand.nextInt(4) + 1;
                if (profilePhotos.size() == 1) break;

                LazyLoadBitmap bmpTask = new LazyLoadBitmap();
                try{
                    Log.d("isloadornot", Boolean.toString(drawable != null));
                    Bitmap bmp = null;
                    if(bmp == null){
                        bmp = bmpTask.execute(p.iconURL).get();
                       drawable = new BitmapDrawable(getResources(), bmp);
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


    @Override
    public boolean onClusterClick(Cluster<Program> cluster) {
        float curZoom = this.getMap().getCameraPosition().zoom;
        if(curZoom >= OCCUR_ZOOM_LEVEL){
            setupRecyclerView(cluster.getItems());


        }
        // Show a toast with some info when the cluster is clicked.
        // String firstName = cluster.getItems().iterator().next().opID;
        // Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

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

        addItems();
        mClusterManager.cluster();

        fl = (FrameLayout)findViewById(R.id.fl);
        setupTagViewpager();
        initFilterButtonListener();
        initHomeButtonListener();

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
            if (tagsSweetSheet.isShow()) {
                tagsSweetSheet.dismiss();
            }
            else
                tagsSweetSheet.toggle();
            }
        });
    }

    private void initHomeButtonListener(){
        FloatingActionButton homeBtn = (FloatingActionButton)findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {mMapManager.initMapFocus(mMapDataManager.programs);
            }
        });
    }

    private void setupTagViewpager() {
        final ArrayList<MenuEntity> list = new ArrayList<>();
        MenuEntity blankEntity = new MenuEntity();
        blankEntity.title = "";
        MenuEntity eraseEntity = new MenuEntity();
        eraseEntity.title = "[CLEAR]";


        eraseEntity.titleColor = 0xffCB1B45;
        list.add(blankEntity); list.add(eraseEntity); list.add(blankEntity);

        for(String str: mMapDataManager.tagList){
            MenuEntity menuEntity = new MenuEntity();
            menuEntity.titleColor = 0xffb3b3b3;
            menuEntity.title = str;
            list.add(menuEntity);
        }
        // attach to FrameLayout
        tagsSweetSheet = new SweetSheet(fl);
        // set list to sweetsheet
        tagsSweetSheet.setMenuList(list);

        // get window height
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        int contentHeight;
        if(mMapDataManager.tagList.size() <= 12) contentHeight = (size.y)/2;
        else if(mMapDataManager.tagList.size() <= 21) contentHeight = (int)(Math.floor(size.y)*0.6);
        else contentHeight = (int)(Math.floor(size.y)*0.7);
        // set the viewpager
        tagsSweetSheet.setDelegate(new ViewPagerDelegate(3, contentHeight));
        // set background effect (dim)
        tagsSweetSheet.setBackgroundEffect(new DimEffect(0.87f));
        // set onclickListener
        tagsSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity) {
                // Empty entity
                if(position == 0 || position == 2) return false;
                else if(position == 1){
                    if(mMapDataManager.filterTagList.size() == 0) return false;
                    // Clear filter
                    mMapDataManager.clearFilter();

                    // Update UI
                    for(MenuEntity me: list){
                        if(me.isChosen){
                            me.titleColor = 0xffb3b3b3;
                        }
                    }
                    tagsSweetSheet.setMenuList(list);
                }
                else {
                    // Update view UI
                    menuEntity.titleColor = (menuEntity.isChosen) ? 0xffb3b3b3 : 0xff303030;
                    menuEntity.isChosen = !menuEntity.isChosen;
                    // Not working method
                    //  ((ViewPagerDelegate) tagsSweetSheet.getDelegate()).notifyDataSetChanged();

                    // Reset list (Bad practice, but it works)
                    tagsSweetSheet.setMenuList(list);

                    // Add/Remove tag to filter
                    if(menuEntity.isChosen)
                        mMapDataManager.filterAddTag(menuEntity.title);
                    else
                        mMapDataManager.filterRemoveTag(menuEntity.title);
                }
                // Reset map according to the filter
                mMapManager.setMapWithFilter(mMapDataManager.programs, mMapDataManager.filterTagList, mClusterManager);

                // If return true, sweetsheet closes, return false does not
                return false;
            }
        });
    }

    private void setupRecyclerView(final Collection<Program> dataI) {
        class LazyVideoListImageLoader implements Runnable{
            ArrayList<MenuEntity> list;
            public LazyVideoListImageLoader (ArrayList<MenuEntity> list){
                this.list = list;
            }

            public void run() {
                List<Program> dataIn = new ArrayList(dataI);
                for(MenuEntity me : list){
                    if(!programSweetSheet.isShow()) return;
                    int tryAgain = 3;
                    Bitmap bmp = null;

//                    Drawable cuteDrawable = mMapDataManager.loadDrawable.get(p.opID);
                    if(bmp == null) {
                        Log.d("nonsense","go"+me.iconURL);
                        while(tryAgain > 0 && bmp == null) {
                            try {
                                LazyLoadBitmap task = new LazyLoadBitmap();
                                bmp = task.execute(me.iconURL).get();
                            } catch (Exception e) {
                                Log.d("nonsense","failed"+ String.valueOf(tryAgain));
                                e.printStackTrace();
                            }
                            tryAgain--;
                        }
                    }
                    if(bmp!=null) {
                        Log.d("nonsense", "string");
                        me.iconBitmap = bmp;
                    }
                }
            }
        }
        Bitmap defaultIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cicon);
        Drawable defaultIconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.cicon);
        final ArrayList<MenuEntity> list = new ArrayList<>();
        for(Program p:dataI) {
            MenuEntity menuEntity = new MenuEntity();
            menuEntity.title = p.opTitle;
            menuEntity.titleColor = 0xff000000;
            menuEntity.iconDrawable = defaultIconDrawable;
            menuEntity.iconBitmap = defaultIconBitmap;
            menuEntity.iconURL = p.iconURL;
            menuEntity.opID = p.opID;
            list.add(menuEntity);
        }



        programSweetSheet = new SweetSheet(fl);
        programSweetSheet.setMenuList(list);
        programSweetSheet.setDelegate(new RecyclerViewDelegate(true));
        programSweetSheet.setBackgroundEffect(new DimEffect(0.77f));
        programSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity1) {
                list.get(position).titleColor = 0xff5823ff;
                ((RecyclerViewDelegate) programSweetSheet.getDelegate()).notifyDataSetChanged();
//                Toast.makeText(StoryMapClusterActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        if (programSweetSheet.isShow()) {
            programSweetSheet.dismiss();
        }
        else programSweetSheet.toggle();
        new Thread((new LazyVideoListImageLoader(list))).start();

//        (new LoadImage()).execute(list);



    }

    // POLLUTION
    @Deprecated
    private class LoadImage extends AsyncTask<List<MenuEntity>, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected Boolean doInBackground(List<MenuEntity>... args) {
            for(MenuEntity me : args[0]) {
                try {
                    Log.d("nonsense", me.iconURL);
                    String url = me.iconURL;
                    URLConnection conn = new URL(url).openConnection();
                    conn.connect();
                    me.iconBitmap = BitmapFactory.decodeStream(conn.getInputStream());
//                    ((RecyclerViewDelegate) programSweetSheet.getDelegate()).notifyDataSetChanged();
                    Log.d("nonsense", "good");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("nonsense", "bAd");
                }

            }
            return true;
        }
        protected void onPostExecute(Boolean flag) {

        }
    }

    private class LazyLoadBitmap extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = null;
            try {
                URLConnection connection = new URL(args[0]).openConnection();
                connection.setUseCaches(true);
                Object response = connection.getContent();
                if(response instanceof Bitmap) {
                    bitmap = (Bitmap) response;
                    Log.d("cache", "good!");
                }
                else {
                    Log.d("cache", "no good!");
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    bitmap = compressBitmap(bitmap);
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap image) {

        }
    }

    public Bitmap compressBitmap(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float ratioWH = height / width;
        float scaleWidth = ((float) 52) / width;
        float scaleHeight = ((float) 52 * ratioWH) / height;
        // create matrix for manipulation
        Matrix matrix = new Matrix();
        // resize the bitmap scale
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
    }


}
