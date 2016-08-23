package com.itri.storymap;
import android.content.Context;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2016/8/8.
 */
public class LoadJSONManager {

    public String loadJSONFromAsset(Context myContext) {
        String json = null;
        try {
            AssetManager mngr = myContext.getAssets();
            InputStream is = mngr.open("RAW_MAP_DATA.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
