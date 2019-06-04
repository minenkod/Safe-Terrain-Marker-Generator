package com.example.gmaps.googlemaps;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class route {

public double lat;
public double lng;
public double endLat;
public double endLng;

public  String distText;
public int dist;

public  route(double inLat, double inLng, String inDistText, int inDist, double inEndLat, double inEndLng) //Constructor method initializing route. Additional properties can be added.
{

lat = inLat;
lng = inLng;
endLat = inEndLat;
endLng = inEndLng;
distText = inDistText;
dist = inDist;
}

public String returnInfo() //Return all properties in a single call. Additional properties can be added here.
{
    String output = "Latitude: " + lat  + "  Longitude: " + lng + " End Latitude: " + endLat + " End Longitude: " + endLng
+   " Distance " + distText;
return  output;
}


    List<LatLng> calcPossitions(GoogleMap googleMap, int distanceFreq) { //Return an array containing locations of tags that are going to be spread out on a road.
//create array size depending
    //if less than 30m discard.
        double new_Lat = 0;
        double new_Lang = 0;
        List<LatLng> posList  = new ArrayList<LatLng>();
        int quantity = dist / distanceFreq; //Number of markers to set.
        double diffLat = (lat - endLat) / quantity;
        double diffLng = (lng - endLng) / quantity;
        double markLat = lat;
        double markLang = lng;
        LatLng markPos;
        //difference between start and end.
        //divide difference by quantity.
        for(int  i =0; i< quantity; i++)
        {
            markLat += diffLat;
           markLang +=diffLng;
            markPos = new LatLng(markLat, markLang);
            posList.add(markPos);
           // posList.add(pos);
            googleMap.addMarker(new MarkerOptions().position(markPos).title("Start"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(markPos));
        }

    return posList;

    }
}