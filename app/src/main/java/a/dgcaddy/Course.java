package a.dgcaddy;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Course extends FragmentActivity implements LocationListener,GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker hole; //declaration of the hole marker so that we know where we need to get to.
    TextView distanceTo;
    TextView Recommendation;
    private final static int INTERVAL = 1000 * 3; //number of seconds for an interval
    Handler mHandler = new Handler();
    int tempThing; // This is used So that we can call the marker creation after the mMap has become != null.'
//    protected LocationManager locationManager;  //Used with the onLocationChanged Method.
    Location lastHolePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        //connects the DistanceTo TextView to the distanceTo TextView in the activity
        distanceTo = (TextView) findViewById(R.id.distanceTo);
        Recommendation = (TextView) findViewById(R.id.Recommendation);
        setUpMapIfNeeded();
        //This was used to Call the onLocationChanged method
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
        tempThing = 0; // restarts the counter for when to create the marker.
        startRepeatingTask();//Starts the mHandlerTask to start continuous calling of methods in mHandlerTask.
    }


    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            //Having tempThing be equal to 4 allows the device 20 seconds to set up the map and get
            //the phones location before trying to find the location and throwing a null pointer exception.
            if(tempThing == 3){
                drawMarker(mMap.getMyLocation());
                lastHolePosition = mMap.getMyLocation();
                LatLng temp = new LatLng(lastHolePosition.getLatitude(),lastHolePosition.getLongitude());
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(lastHolePosition.getLatitude(),lastHolePosition.getLongitude()),18));}
            if(tempThing > 4 && hole == null){drawMarker(lastHolePosition);}

            //This is to help you know that the method is being called properly and also lets you know the marker
            //hasn't been dropped/created.
            if(getDistance(mMap.getMyLocation(), hole) == -1){
               distanceTo.setText("unknown");
               Recommendation.setText(discRec(getDistance(mMap.getMyLocation(),hole)));
            }
            else {
                //gets the distance between the marker and your position.
                distanceTo.setText("" + getDistance(mMap.getMyLocation(), hole));
                Recommendation.setText(discRec(getDistance(mMap.getMyLocation(),hole)));
            }
            tempThing ++; // used for incrementing the tempThing to get to 4 for creating the marker.
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask(){
        mHandlerTask.run();
    }

    void stopRepeatingTask(){
        mHandler.removeCallbacks(mHandlerTask);
    }
    public  String discRec(double distance){
        if(distance == -1){ return null; }
        else if(0 <= distance && distance < 11){ return "A Putter";}
        else if(11 <= distance && distance < 40){ return "A Mid or Distance driver";}
        else if(40 <= distance){return "A Distance or Fairway driver";}
        else{return null;}
    }

    /**
     * This method is used to get the distance between two locations being passed in by the parameters
     *
     * @param me is the location your are at/ the starting point of the distanceTo method
     * @param hole is the marker that you are trying to find out how far you are from.
     * @return This will return a -1 if the marker is null, and returns the distance between the two points as a double.
     */
    public double getDistance(Location me, Marker hole){
        double temp;// temp variable that is returned
        //This allows the method to not return a null pointer error when the Marker is not created or doesn't exist.
        if(hole == null){return -1;}
        Location endPoint = new Location("endPoint");       //creates a location to use for finding distance.
        endPoint.setLatitude(hole.getPosition().latitude);  //sets the Latitude of the location to the Markers Latitude.
        endPoint.setLongitude(hole.getPosition().longitude);//sets the Longitude of the location to the Markers Longitude.
        temp = me.distanceTo(endPoint); // assigns the distanceTo to the temp variable.
        int temp2 = ((int) temp);
        return temp2; //returns the distance between the two points.
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        //This was used to Call the onLocationChanged method
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0,this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setOnMarkerDragListener(this);         //Drag listener for the Hole Marker.
//            map.setOnMapLongClickListener(this);      //These two are going to be used later to Drop the pin
//            map.setOnMapClickListener(this);          //Instead of creating the pin automatically.
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);//Makes the google map activity have satellite view.


    }

    /**
     * This is where you create the hole marker with the information of the location passed in
     * as the location parameter.
     *
     * @param location  This is the location being passed in to make the marker.
     */
    private void drawMarker(Location location)throws NullPointerException{
        if(location == null){
            throw new NullPointerException("location is null");
        }
        //This Sets the position of the hole marker in front of what ever location passed in .0005 north of the location.
        LatLng holePosition = new LatLng(location.getLatitude()+.0005,location.getLongitude() );

        //This creates the marker using the hole position above this also sets
        //the snippet of the marker to a custom message at the start of the app.
        hole = mMap.addMarker(new MarkerOptions().position(holePosition)
                .title("Drag this marker to the end of the hole you are playing.")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.diskmarker)));
        hole.showInfoWindow();

    }

    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
//        hole.setPosition(marker.getPosition());
    }

    @Override
    public void onMarkerDrag(Marker marker) {

//        hole.setPosition(marker.getPosition());

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        //updates the location of the marker when it is done being dragged.
        hole.setPosition(marker.getPosition());
        lastHolePosition.setLatitude(marker.getPosition().latitude);
        lastHolePosition.setLongitude(marker.getPosition().longitude);
        if (hole.isInfoWindowShown() == true){hole.hideInfoWindow();}
    }

}
