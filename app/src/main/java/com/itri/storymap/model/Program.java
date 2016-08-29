/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itri.storymap.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.itri.storymap.R;

public class Program implements ClusterItem {
    public String opID;
    public String opTitle;
    private String opType;
    private String channelID;
    private int duration;
    private int popularity;
    private int createDate;
    public double lat;
    public double lon;
    public String iconURL;
    private int photoCount;
    private String tagList;

    public LatLng mPosition;
    public int profilePhoto;
    public String [] tags;
    public boolean isLoad;

    public Program() {
        isLoad = false;

    }
    public Program(String opID, String opTitle, String iconURL, double lat, double lon, String tagList){
        this.opID = opID;
        this.opTitle = opTitle;
        this.iconURL = iconURL;
        this.lat = lat;
        this.lon = lon;
        this.tagList = tagList;
        isLoad = false;

    }

    /* convert the GPS format to LatLng, should be called after object created */
    public void convertToLatLng(){
        this.mPosition = new LatLng(lat,lon);
    }

    /* parse tags from tagList */
    public void parseTags(){
        if(tagList.length() > 0)
            this.tags = tagList.split(",");
        else{
            this.tags = new String[1];
            tags[0] = "%NOTAGS%";
        }
    }

    /* set profilephoto */
    public void setPhoto(){
        profilePhoto = R.drawable.ic_launcher;
    }

    public int getPopularity(){ return popularity;}

    @Override
    public LatLng getPosition() {
        return this.mPosition;
    }


}
