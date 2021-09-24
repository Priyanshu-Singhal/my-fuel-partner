package com.example.myfuelpartner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myfuelpartner.databinding.ActivityDriverMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button LogoutBtn;
    Button RegistrationBtn;

    LocationManager locationManager;
    Marker PickUpMarker;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Boolean currentLogoutDriverStatus = false;

    DatabaseReference AssignedCustomerRef ,AssignedCustomerPickUpRef;
    ValueEventListener AssignedCustomerPickUpRefListner;

    private TextView txtName, txtPhone ,txtFuel , txtQuantity;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;


    String driverId ,customerId ="";

    private ActivityDriverMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        driverId = mAuth.getCurrentUser().getUid();

        LogoutBtn = findViewById(R.id.driver_logout_btn);
        RegistrationBtn = findViewById(R.id.registration_btn);

        txtName = findViewById(R.id.name_customer);
        txtPhone = findViewById(R.id.phone_customer);
        txtFuel = findViewById(R.id.fuel_customer);
        txtQuantity = findViewById(R.id.quantity_customer);
        profilePic = findViewById(R.id.profile_image_customer);
        relativeLayout = findViewById(R.id.rel2);




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        RegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(DriverMapsActivity.this, DriverProfile.class);
                intent.putExtra("type", "Drivers");
                startActivity(intent);
            }
        });

         LogoutBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 currentLogoutDriverStatus = true;
                 DisconnectDriver();
                 mAuth.signOut();
                 Intent intent = new Intent(DriverMapsActivity.this , Welcome.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 startActivity(intent);
                 finish();
             }
         });

         getAssignedCustomerRequest();



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
                LatLng latLng = new LatLng(latitude , longitude);
                //Instantiate the class,Geocoder
                    Geocoder geocoder =new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude , longitude ,1 );
                        String str = addressList.get(0).getLocality()+",";
                        str+= addressList.get(0).getCountryName();

                        mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    DatabaseReference DriverAvailablityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
                    GeoFire geoFireAvailable = new GeoFire(DriverAvailablityRef);

                    DatabaseReference DriverWorking = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
                    GeoFire geoFireWorking = new GeoFire(DriverWorking);

                  switch (customerId)
                  {
                      case "":
                          geoFireWorking.removeLocation(userId);
                          geoFireAvailable.setLocation(userId , new GeoLocation(latitude,longitude));
                          break;

                      default:
                          geoFireAvailable.removeLocation(userId);
                          geoFireWorking.setLocation(userId , new GeoLocation(latitude,longitude));
                          break;
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
                    LatLng latLng = new LatLng(latitude, longitude);
                    //Instantiate the class,Geocoder
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }




                }
            });
        }   }

    private void getAssignedCustomerRequest()
    {
        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverId).child("CustomerRideId");
        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    customerId =  snapshot.getValue().toString();
                    GetAssignedCustomerPickUpLocation();
                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();
                }
                else
                {
                    customerId = "";
                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }
                    if (AssignedCustomerPickUpRefListner != null)
                    {
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListner);
                    }
                    relativeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }

    private void GetAssignedCustomerPickUpLocation()
    {
        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
                .child(customerId).child("l");
      AssignedCustomerPickUpRefListner =   AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    List<Object> customerLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat =0;
                    double LocationLng =0;

                    if(customerLocationMap.get(0) != null)
                    {//getting latitude from the data base and converting it into double datatype
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());

                    }
                    if(customerLocationMap.get(1) != null)
                    {//getting longitude from the data base and converting it into double datatype
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());

                    }
                    //adding marker to driver's position
                    LatLng DriverLatLng = new LatLng(LocationLat , LocationLng);
                    //adding marker to driver's position
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng)
                            .title("Customer Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_icon)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });


    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;



  }
    
    public void onLocationChanged(Location location)
    {
        if(getApplicationContext() != null)
        {
            //getting the updated location


            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriversAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireAvailability = new GeoFire(DriversAvailabilityRef);

            DatabaseReference DriversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireWorking = new GeoFire(DriversWorkingRef);

            switch (customerId)
            {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
       if(!currentLogoutDriverStatus)
       {
           DisconnectDriver();
       }
    }

    private void DisconnectDriver()
    {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference DriverAvailablityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFire = new GeoFire(DriverAvailablityRef);
        geoFire.removeLocation(userId);
    }

    private void getAssignedCustomerInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String fuel = dataSnapshot.child("fuel").getValue().toString();
                    String quantity = dataSnapshot.child("quantity").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtFuel.setText(fuel);
                    txtQuantity.setText(quantity+" Ltr");


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}