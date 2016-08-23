package com.itri.storymap;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itri.storymap.model.Program;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by user on 2016/8/10.
 */
public class MapDataManager {
    public String MAP_DATA;
    public List<Program> programs = new ArrayList<Program>();
    public Map<String, Drawable> loadDrawable = new HashMap<String, Drawable>();

    public boolean initLoadJSONFromAsset(Context myContext) {
        String json;
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
            return false;
        }
        MAP_DATA =  json;
        return true;
    }

    public List<Program> getProgramListItem (){

//        Type collectionType = new TypeToken<Collection<Program>>(){}.getType();
//        Collection<Program> programs = gson.fromJson(MAP_DATA, collectionType);
        try {
            JSONObject jObject = new JSONObject(MAP_DATA);
            JSONArray jArray = jObject.getJSONArray("RAW_MAP_DATA");
            for(int i = 0; i < jArray.length(); ++i){
                JSONObject mProgram = jArray.getJSONObject(i);
//                Program p = new Program(mProgram.getString("opID"), mProgram.getString("opTitle"),mProgram.getString("iconURL"), mProgram.getDouble("lat"),mProgram.getDouble("lon"), mProgram.getString("tagList"));

                double lat = mProgram.getDouble("lat");
                double lon = mProgram.getDouble("lon");

                if(lat == 0.0 && lon == 0.0){
                    Log.d("nonsense", Double.toString(lat) + ' ' + Double.toString(lon));
                }

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                // Convert from JSON to Program Object
                programs.add(gson.fromJson(mProgram.toString(), Program.class));
                // Convert into LagLng form
                programs.get(programs.size()-1).convertToLatLng();
                programs.get(programs.size()-1).parseTags();
                programs.get(programs.size()-1).setPhoto();


            }
        }
        catch (JSONException e){
            Log.e("JSON exception", "Something wrong with program JSON");
        }
        return programs;

    }









}
