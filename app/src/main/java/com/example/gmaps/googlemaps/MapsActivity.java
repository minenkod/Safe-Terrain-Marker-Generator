package com.example.gmaps.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    String myAPIKEY = Cred.APIKEY; //PASTE YOUR API KEY HERE

    private GoogleMap mMap;
    Button search;
    EditText etTo;
    EditText etFrom;
    EditText etFreq;
    ListView dirLv;
    int totalSize =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        search = (Button) findViewById(R.id.btnSearch);
         etTo = (EditText) findViewById(R.id.etTo);
          etFrom = (EditText) findViewById(R.id.etFrom);
          etFreq = (EditText) findViewById(R.id.etFreq);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        search.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                //Available methods of transport: WALKING, CYCLING, TRANSIT, DRIVING
                    String result = downloadJson(getURL("WALKING")); //Download raw JSON.
                 List<route> routes =  parseJson(result);  //Get list populated with route objects.
            }
        });
    }


//https://postal-code.co.uk/postcode/London   Useful  postal code viewer
String getURL(String method) //Method encodes url to be suitable for querying the api.
{
    String encodedQuery = "";
    String to = etTo.getText().toString(); //Get location input from the editText.
    String from = etFrom.getText().toString();
    to = to.replaceAll(" ", ""); //Strip all spaces
    from = from.replaceAll(" ", ""); //strip all spaces

   String baseQuery = "https://maps.googleapis.com/maps/api/directions/json?origin="+  from + "&destination=" + to + "&mode=" + method + "&key=" + myAPIKEY; //Replace API key here.
    //  String baseQuery = "https://maps.googleapis.com/maps/api/directions/json?origin="+  from + "&destination=" + to + "&mode=" + method + "&key=AIzaSyAjtgXdu5VL7zB4xIKKGVK9a9cmWwBb4Q0"; //Replace API key here.
   encodedQuery = baseQuery.replace(" ", "+"); //add a plus between spaces to allow multiple words when searching.
    return encodedQuery;
}

    void writeList( ) { //Method saves stringbuilder to the file with the number of all the
       try
       {
        //go through all the lists and save csv coordinates.
           File testFile = new File(this.getExternalFilesDir(null), "Output.csv");
           if (!testFile.exists())
               testFile.createNewFile();
           BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, false /*append*/));
                writer.write("size:" + totalSize + ",\n");

                writer.write(markerBuilder.toString());

           writer.close();
           MediaScannerConnection.scanFile(this,
                   new String[]{testFile.toString()},
                   null,
                   null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    StringBuilder markerBuilder = new StringBuilder();

    void saveList(List<LatLng> posList)
    {
        for(int i =0 ; i< posList.size(); i++)
        {
            double lat = posList.get(i).latitude;
            double lng = posList.get(i).longitude;
            markerBuilder.append(lat + "," + lng  + "\n");
        }
    }

    String result;
    public String downloadJson(final String url) { //Simple method downloading raw json and returning result as a string.
        result = ""; //Reset variable for new results.
        try {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        InputStream stream = (InputStream) new URL(url).getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                        String line = "";
                        while (line != null) {
                            result += line;      //Read line by line and add to the result string variable.
                            line = reader.readLine();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            t.join();

        } catch (Exception e) {
        }
        return result;
    }

    List<route> parseJson(String input) {
        List<route> routeList = new ArrayList<route>();
        markerBuilder.setLength(0);
        totalSize = 0;
    try {
      //  List<Route> routes=new ArrayList<Route>();
        input = input.replace("null", ""); //Remove null word at the begging on response.
        JSONObject mainJSON=new JSONObject(input);
        JSONArray jarray1=mainJSON.getJSONArray("routes"); //Json parents with '[' indicates start of an array.
        JSONObject jobj1=jarray1.getJSONObject(0); //Grab first route.
        JSONArray jarray2=jobj1.getJSONArray("legs");
        JSONObject jobj2=jarray2.getJSONObject(0);//Grab first leg
        JSONArray stepArray=jobj2.getJSONArray("steps"); //Grab all steps into an array

        for (int i=0;i<stepArray.length();i++){ //Iterate through all steps.
            JSONObject jobj5=stepArray.getJSONObject(i);
            JSONObject disOBJ=jobj5.getJSONObject("distance"); //Get distance of the particular step. eg 34m or 2km.
      //      JSONObject durOBJ=jobj5.getJSONObject("duration");  //Uncomment to add duration to the object.
            JSONObject endOBJ=jobj5.getJSONObject("end_location");
      //      JSONObject polOBJ=jobj5.getJSONObject("polyline"); //Polyline can be decoded to add to a map.
            JSONObject startOBJ=jobj5.getJSONObject("start_location");
       //     String startAddress=jobj2.getString("start_address");
         //   String end_address =jobj2.getString("end_address");
            if (jobj5.has("maneuver")){   //In the event user decided to maneuver then recalculate. This is not needed but exists for potential use.
              //  route.maneuver=new Maneuver(jobj5.getString("maneuver"));
            }
           String distance = disOBJ.getString("text"); //Distance with metric. Eg 24m.
           int dist = disOBJ.getInt("value");         //The distance as a value. Calculations could be performed on.
            //Values of start and end coordinates captured below.
            double lat = startOBJ.getDouble("lat"); //Start lat and lng.
            double lng = startOBJ.getDouble("lng");
            double endLat = endOBJ.getDouble("lat"); //End lat and lng
            double endLng = endOBJ.getDouble("lng");
            int frequency = Integer.parseInt(etFreq.getText().toString());

            route newRoute = new route(lat, lng, distance, dist, endLat, endLng );  //Create route object holding properties parsed.
            List<LatLng> positionList = newRoute.calcPossitions(mMap,frequency);
            saveList(positionList);
            totalSize+=positionList.size();
            routeList.add(newRoute);
        }

        Toast.makeText(this, "Paths Generated.", Toast.LENGTH_SHORT).show();

        writeList();
    } catch (JSONException e) {
        e.printStackTrace();
        Toast.makeText(this, "Locations too broad or incorrect. Please enter precise postcode.", Toast.LENGTH_SHORT).show();  //0 Routes has been found and exception is caught.
    }
    return  routeList;
}
//0.2 km between tags

    @Override
    public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    }

    }

