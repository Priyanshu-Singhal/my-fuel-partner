package com.example.myfuelpartner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myfuelpartner.databinding.ActivityCustomerMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button LogoutBtn;

    Button CallBtn;
    Button OrderBtn;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference CustomerDatabaseRef;
    LatLng latLng;
    LatLng CustomerPickUpLocation;
    DatabaseReference DriverAvailableRef;
    DatabaseReference DriverLocationRef;
    DatabaseReference DriversRef;
    int radius =1;
    Boolean driverFound = false ,requestType = false;
    String driverFoundId;
    String customerId;
    Marker DriverMarker , PickUpMarker;
    GeoQuery geoQuery;
    ValueEventListener DriverLocationRefListner;

    private TextView txtName, txtPhone, txtCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;



    LocationManager locationManager;



    Boolean currentLogoutDriverStatus = false;




















    private ActivityCustomerMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");





        LogoutBtn = findViewById(R.id.customer_logout_btn);
        OrderBtn = findViewById(R.id.customer_orders_btn);
        CallBtn = findViewById(R.id.customer_call_btn);

       txtName = findViewById(R.id.name_driver);
       txtPhone = findViewById(R.id.phone_driver);
       txtCarName = findViewById(R.id.car_name_driver);
       profilePic = findViewById(R.id.profile_image_driver);
       relativeLayout = findViewById(R.id.rel1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        OrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(CustomerMapsActivity.this, CustomersOrder.class);
                intent.putExtra("type", "Customers");
                startActivity(intent);
            }
        });


        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisconnectCustomer();
                mAuth.signOut();
               // currentLogoutDriverStatus = true;
                Intent intent = new Intent(CustomerMapsActivity.this, Welcome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });




        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        CallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(requestType)
                {
                 requestType =false;
                 geoQuery.removeAllListeners();
                 DriverLocationRef.removeEventListener(DriverLocationRefListner);

                 if(driverFound != null)
                 {
                     DriversRef = FirebaseDatabase.getInstance().getReference()
                             .child("Users").child("Drivers").child(driverFoundId).child("CustomerRideId");
                     DriversRef.removeValue();

                     driverFoundId = null;
                 }
                 driverFound = false;
                 radius = 1;
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(customerId);

                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }
                    if (DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }
                    CallBtn.setText("Call a Fuel Tank");
                    relativeLayout.setVisibility(View.GONE);
                }
                else
                {
                   requestType = true;

                   String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerId, new GeoLocation(latLng.latitude, latLng.longitude));

                    CustomerPickUpLocation = new LatLng(latLng.latitude, latLng.longitude);
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation)
                            .title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_icon)));

                    CallBtn.setText("Getting your Driver...");
                    getClosestDriverCab();
                }
            }
        });




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //chech whether the network  provider is available
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {


            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    //get the latitude
                    double latitude = location.getLatitude();
                    //get Longitude
                    double longitude = location.getLongitude();
                    //instantiate the class LatLng
                    latLng = new LatLng(latitude , longitude);
                    //Instantiate the class,Geocoder
                    Geocoder geocoder =new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude , longitude ,1 );
                        /*String str = addressList.get(0).getLocality()+",";
                        str+= addressList.get(0).getCountryName();
*/
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            });

        }
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //get the latitude
                    double latitude = location.getLatitude();
                    //get Longitude
                    double longitude = location.getLongitude();
                    //instantiate the class LatLng
                    latLng = new LatLng(latitude, longitude);
                    //Instantiate the class,Geocoder
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                       /* String str = addressList.get(0).getLocality() + ",";
                        str += addressList.get(0).getCountryName();
*/
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void DisconnectCustomer()
    {
        customerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoFire.removeLocation(customerId);
    }

    private void getClosestDriverCab()
    {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestType)
                {
                    driverFound = true;
                    driverFoundId = key;
                    DriversRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driverFoundId);

                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideId", customerId);
                    DriversRef.updateChildren(driverMap);

                    GettingDriverLocation();
                    CallBtn.setText("Looking for Tank's Location");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
               if(!driverFound)
               {
                   radius = radius + 1;
                   getClosestDriverCab();
               }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation()
    {
       DriverLocationRefListner = DriverLocationRef.child(driverFoundId).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && requestType) {
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                            double LocationLat = 0;
                            double LocationLng = 0;
                            CallBtn.setText("Driver Found");
                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInformation();

                            if (driverLocationMap.get(0) != null) {//getting latitude from the data base and converting it into double datatype
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                            }
                            if (driverLocationMap.get(1) != null) {//getting longitude from the data base and converting it into double datatype
                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());

                            }

                            //adding marker to driver's position
                            LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);

                            //if driver cancelled the request of the customer then we need to remove that marker from that driver's position
                            if (DriverMarker != null) {
                                DriverMarker.remove();
                            }


                            //getting latitude and longitude of customer for calculating distance
                            Location location1 = new Location("");
                            location1.setLatitude(latLng.latitude);
                            location1.setLongitude(latLng.longitude);

                            //getting latitude and longitude of driver for calculating distance
                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            //calculating distance beteen the driver and the customer and storing it into the Distance variable
                            float Distance = location1.distanceTo(location2);

                            if (Distance < 90)
                            {
                                CallBtn.setText("Driver Arrived");
                            }
                            else
                            {
                                CallBtn.setText("Driver found :" + String.valueOf(Distance));
                            }



                            //adding marker to driver's position
                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.tank_icon)));


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
    @Override
    protected void onStop() {
        super.onStop();

    }

    private void getAssignedDriverInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverFoundId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
