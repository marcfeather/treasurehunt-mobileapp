package com.th.monicadzhaleva.treasurehunt.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.th.monicadzhaleva.treasurehunt.CustomList;
import com.th.monicadzhaleva.treasurehunt.Objects.TriviaQuestion;
import com.th.monicadzhaleva.treasurehunt.R;
import com.th.monicadzhaleva.treasurehunt.Treasure;
import com.th.monicadzhaleva.treasurehunt.User;
import com.th.monicadzhaleva.treasurehunt.UserToTreasure;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    public User activeUser;
    private TextView userGreeting;
    private TextView userLevel;
    private TextView userExperience;
    private ImageButton huntButton;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    MarkerOptions currLocOptions;
    LocationRequest mLocationRequest;
    int locChangeCount=0;
    final ArrayList<Treasure> treasuresList = new ArrayList<Treasure>();
    public HashMap<String,Integer> clanNamesImages=new HashMap<>();
    public final HashMap<String,Treasure> treasureMap=new HashMap<String,Treasure>();
    public final HashMap<String,String> treasureMapUrls=new HashMap<String,String>();
    FirebaseDatabase database;
    protected ImageView avatar;
    Button filterButton;
    Button collectButton;
    ImageView treasureImage;
    TextView treasureOwnerInfo;
    TextView treasureName;
    TextView treasureInfo;
    ListView treasureOwnerClanList;
    RadioGroup rg;
    LatLng previousRecorderLatLng;
    String filterCategory;
    String alreadyCollectedTreasures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        database = FirebaseDatabase.getInstance();
        alreadyCollectedTreasures="";
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        populateClanMap();
        setSupportActionBar(myToolbar);
        getUserDetails();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //respond to menu item selection
        if(item.getItemId()==R.id.treasuresfeedback) {
            getNearbyTreasures();
        }
        switch (item.getItemId()) {
            case R.id.userprofile:
                Intent userprofileactivity = new Intent(MapsActivity.this, UserActivity.class);
                userprofileactivity.putExtra("username", activeUser.getUsername());
                userprofileactivity.putExtra("currentActiveUser", activeUser.getUsername());
                startActivity(userprofileactivity);
                return true;
            case R.id.messages:
                Intent usermessagesactivity = new Intent(MapsActivity.this, MyMessagesActivity.class);
                usermessagesactivity.putExtra("username", activeUser.getUsername());
                startActivity(usermessagesactivity);
                return true;
            case R.id.categories:
                showCategoriesAlertDialog();
                return true;
            case R.id.recommendations:
                Intent recommendationsactivity = new Intent(MapsActivity.this, RecommendationsActivity.class);
                recommendationsactivity.putExtra("username", activeUser.getUsername());
                getNearbyTreasures();
                startActivity(recommendationsactivity);
                return true;
            case R.id.ranking:
                Intent rankingactivity = new Intent(MapsActivity.this, LeaderboardActivity.class);
                rankingactivity.putExtra("username", activeUser.getUsername());
                startActivity(rankingactivity);
                return true;
            case R.id.treasuresfeedback:
                Intent feedbackactivity = new Intent(MapsActivity.this, FeedbackActivity.class);
                getNearbyTreasures();
                feedbackactivity.putExtra("map", treasureMapUrls);
                startActivity(feedbackactivity);
                return true;
            case R.id.logout:
                Intent splashscreen = new Intent(MapsActivity.this, SplashScreen.class);
                startActivity(splashscreen);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.googlestyle);
        mMap.setMapStyle(styleOptions);
        mMap.setOnMarkerClickListener(this);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
     //   mockLocation();
        float distanceInMeters=0;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(mLastLocation!=null) {
            distanceInMeters = mLastLocation.distanceTo(location);
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        currLocOptions = new MarkerOptions();
        currLocOptions.position(latLng);
        currLocOptions.title(activeUser.getUsername());
        currLocOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.person));
        mCurrLocationMarker = mMap.addMarker(currLocOptions);


        //move map camera
        if(distanceInMeters>10) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void getUserDetails() {
        // Get user details from login intent screen
        activeUser = new User();
        final DatabaseReference activeUserRef = database.getReference().child("users").child(getIntent().getStringExtra("username"));
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeUser = dataSnapshot.getValue(User.class);
                setUserToolbar(activeUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        activeUserRef.addValueEventListener(postListener);
    }

    /* Set the top user toolbar */

    public void setUserToolbar(final User user) {
        userGreeting = (TextView) findViewById(R.id.usergreeting);
        userLevel = (TextView) findViewById(R.id.level);
        userExperience = (TextView) findViewById(R.id.experience);
        huntButton = (ImageButton) findViewById(R.id.huntButton);
        avatar= (ImageView) findViewById(R.id.avatarView);

        String uri = "@drawable/"+user.getAvatar();  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        if(imageResource!=0)
        {
            Drawable res = getResources().getDrawable(imageResource);
            if(res!=null) {
                avatar.setImageDrawable(res);
                avatar.setTag(user.getAvatar().replace("avatar",""));
            }}else
        {
            Log.i("Does not exist","Image does not exist");
        }
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userprofileactivity = new Intent(MapsActivity.this, UserActivity.class);
                userprofileactivity.putExtra("username", activeUser.getUsername());
                userprofileactivity.putExtra("currentActiveUser", activeUser.getUsername());
                startActivity(userprofileactivity);            }
        });


        huntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Hunt", "Hunt");
                getNearbyTreasures();


            }
        });

       // mockLocation();

        userGreeting.setText("Hello, " + user.getUsername());
        userLevel.setText("Level: " + user.getLevel());
        userExperience.setText("Experience: " + user.getExperience());
    }

    private void mockLocation() {
        MarkerOptions mockLocationOpt = new MarkerOptions();
        LatLng latLng = new LatLng(51.523171, -0.043183);
        mockLocationOpt.position(latLng);
        mCurrLocationMarker = mMap.addMarker(mockLocationOpt);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

    }

    public void getNearbyTreasures() {
        filterCategory="";
        mMap.clear();
        treasuresList.clear();

        final DatabaseReference treasuresRef = database.getReference().child("treasures");
        treasuresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Treasure treasure = snapshot.getValue(Treasure.class);
                    treasureMap.put(treasure.getName(),treasure);
                    treasureMapUrls.put(treasure.getName(),treasure.getImageUrl());
                    treasuresList.add(treasure);
                }

                injectTreasuresToMap(treasuresList,false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setCurrLoc() {
        mCurrLocationMarker = mMap.addMarker(currLocOptions);
    }

    /* Add all treasures in map */

    public void injectTreasuresToMap(final ArrayList<Treasure> treasuresList, final boolean filtered) {
        String username = activeUser.getUsername();
        for (final Treasure treasure : treasuresList) {
            if (treasure.getName() != null) {
                final LatLng latLng = new LatLng(treasure.getLatitude(), treasure.getLongitude());
                final DatabaseReference userToTreasureRef = database.getReference().child("user_to_treasure").child(username).child(treasure.getName());
                userToTreasureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserToTreasure userToTreasure = dataSnapshot.getValue(UserToTreasure.class);
                        if(userToTreasure!=null) {
                            if (userToTreasure.isCollected()) {
                                // user has already collected the treasure: do not display!
                                Log.i(treasure.getName(), " already collected");
                                if(treasure.getCategory()!=null) {
                                    if(alreadyCollectedTreasures.equals(""))
                                    {
                                        alreadyCollectedTreasures=treasure.getCategory();
                                    }else
                                    {
                                        String[] categories=treasure.getCategory().split((","));
                                        for(String category: categories) {
                                                alreadyCollectedTreasures = alreadyCollectedTreasures + "," + category;
                                        }
                                    }
                                }

                                return;
                            }else
                            {
                                // user has not collected the treasure
                                addTreasureMarker(treasure,latLng,filtered);

                            }
                        }else {
                            addTreasureMarker(treasure,latLng,filtered);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }
        }
    }

    /* Add a treasure marker to the map */

    public void addTreasureMarker(Treasure treasure, LatLng latLng,boolean filtered) {
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(treasure.getName()));
            newMarker.setSnippet("Type: " + treasure.getType() + " | Points: " + treasure.getPoints());
            newMarker.setTag(treasure.getPoints());

        if(!filtered) {
            if (treasure.getType() != null) {
                if (treasure.getType().equals("bronze")) {
                    newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bronze));
                } else if (treasure.getType().equals("silver")) {
                    newMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.silver)));
                } else if (treasure.getType().equals("gold")) {
                    newMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.gold)));
                }
            } else {
                Log.i(treasure.getName(), "No type defined for this treasure");
            }
        }else
        {

            String category=filterCategory.replace(" ","").toLowerCase();

            String uri = "@drawable/"+category+"icon";  // where myresource (without the extension) is the file
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            if(imageResource!=0)
            {
                Drawable res = getResources().getDrawable(imageResource);
                if(res!=null) {
                    newMarker.setIcon(BitmapDescriptorFactory.fromResource(imageResource));
                }}else
            {
                Log.i("Does not exist","Image does not exist");
            }
        }
    }

    /* When a treasure marker is clicked */

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("CLICK", "Marker was clicked");
        Log.i("Marker Position: ", marker.getPosition().toString());
        if (!marker.getTitle().equals(activeUser.getUsername())) {
            /* Marker Location */
            LatLng markerPos = marker.getPosition();
            final Location markerLoc = new Location("");
            markerLoc.setLatitude(markerPos.latitude);
            markerLoc.setLongitude(markerPos.longitude);
            final Treasure treasureClicked= treasureMap.get(marker.getTitle());

        /* Show custom dialog about location */
            final Dialog dialog = new Dialog(MapsActivity.this);
            dialog.setContentView(R.layout.custom_dialog_treasure);
            dialog.setTitle(marker.getTitle());

            dialog.setCancelable(true);

            // now that the dialog is set up, it's time to show it
            dialog.show();

            collectButton = (Button) dialog.findViewById(R.id.buttonCollect);

            treasureImage = (ImageView) dialog.findViewById(R.id.imageViewTreasure);
            if(treasureClicked!=null)
            {
                treasureName = (TextView) dialog.findViewById(R.id.treasureName);
                treasureName.setText(treasureClicked.getName());
                treasureInfo = (TextView) dialog.findViewById(R.id.treasureInfo);
                treasureInfo.setText(treasureClicked.getInfo());
                treasureOwnerInfo = (TextView) dialog.findViewById(R.id.treasureOwnerInfoText);
                if(treasureClicked.getOwner()==null)
                {
                    treasureOwnerInfo.setText("No one owns this treasure. Be the first one to collect it!");
                }else {
                    treasureOwnerInfo.setText(treasureClicked.getOwner());
                }
                treasureOwnerClanList = (ListView) dialog.findViewById(R.id.treasureClanInfoText);
                if(treasureClicked.getOwnerClan()!=null) {
                    String userclan = treasureClicked.getOwnerClan();
                    String[] userClans = {userclan};
                    if(clanNamesImages.get(userclan)!=null) {
                        int userClanImage = clanNamesImages.get(userclan);
                        Integer[] userImages = {userClanImage};
                        CustomList adapter = new
                                CustomList(MapsActivity.this, userClans, userImages);
                        treasureOwnerClanList.setAdapter(adapter);
                    }
                }else{
                    TextView treasureOwnerClan = (TextView) dialog.findViewById(R.id.treasureClanInfo);
                    treasureOwnerClan.setVisibility(View.INVISIBLE);
                }
                if(treasureClicked.getImageUrl()!=null)
                {
                    Picasso.get().load(treasureClicked.getImageUrl()).into(treasureImage);

                }
                // set up listener to filter button
                collectButton.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        collectTheTreasure(dialog, marker, markerLoc, treasureClicked);
                    }
                });
            }
            }

        return true;
    }

    /* collect treasure */

    private void collectTheTreasure(final Dialog dialogBig, final Marker marker, Location markerLoc, final Treasure treasure) {
           /* User Current Location */
        if (mCurrLocationMarker != null) {
            LatLng userPos = mCurrLocationMarker.getPosition();
            Location userLoc = new Location("");
            userLoc.setLatitude(userPos.latitude);
            userLoc.setLongitude(userPos.longitude);

            float distanceInMeters = userLoc.distanceTo(markerLoc);
            final Dialog dialogOwner = new Dialog(MapsActivity.this);

            if (distanceInMeters <= 10000000) // if distance in meters is less than 1000
            {
                // User can collect the treasure
                Log.i("Alert", "User can collect the treasure");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Dear traveller, you are close enough to collect the treasure!");
                builder.setTitle("Collect the treasure: " + marker.getTitle());
                builder.setIcon(R.drawable.icon);
                builder.setPositiveButton("Collect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogBig.hide();

                        final UserToTreasure user_to_treasure = new UserToTreasure();
                        user_to_treasure.setCollected(true);
                        final DatabaseReference userToTreasureRef = database.getReference().child("user_to_treasure");
                        final DatabaseReference treasureRef = database.getReference().child("treasures").child(treasure.getName());


                        // Check if treasure has an owner
                        if(treasure.getOwner()!=null)
                        {
                            /* The treasure has an owner already ..
                            * Allow the user to challenge them */

                            // Show challenge dialog
                            final Dialog challengeDialog = new Dialog(MapsActivity.this);
                            challengeDialog.setContentView(R.layout.custom_dialog_challenge);
                            challengeDialog.setTitle(marker.getTitle());
                            challengeDialog.setCancelable(true);

                            // Show the current existing owner
                            TextView ownerView = challengeDialog.findViewById(R.id.textOwnerName);
                            ownerView.setText(treasure.getOwner());
                            ownerView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userprofileactivity = new Intent(MapsActivity.this, UserActivity.class);
                                    userprofileactivity.putExtra("username", treasure.getOwner());
                                    userprofileactivity.putExtra("currentActiveUser", activeUser.getUsername());
                                    startActivity(userprofileactivity);
                                }
                            });
                            // now that the dialog is set up, it's time to show it
                            challengeDialog.show();

                            final Button challengeButton=challengeDialog.findViewById(R.id.challengeButton);
                            challengeButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    // show trivia dialog
                                    final Dialog triviaDialog = new Dialog(MapsActivity.this);
                                    triviaDialog.setContentView(R.layout.custom_dialog_trivia);
                                    triviaDialog.setTitle(marker.getTitle()+" Trivia Battle");
                                    triviaDialog.setCancelable(true);
                                    triviaDialog.show();

                                    setUpTrivia(treasure, triviaDialog, challengeDialog, treasure.getName(),marker,userToTreasureRef,user_to_treasure,treasureRef);
                                }
                            });
                            Button cancelButton=challengeDialog.findViewById(R.id.cancelButton);
                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    // hide dialog
                                    challengeDialog.cancel();
                                }
                            });
                        }else
                        {
                            /* The treasure does not have an owner
                            be the first one to collect it
                             */

                            // Mark treasure as "collected"
                            userToTreasureRef.child(activeUser.getUsername()).child(marker.getTitle()).setValue(user_to_treasure);

                            // Set the owner of the treasure as the current active user
                            treasureRef.child("owner").setValue(activeUser.getUsername());
                            treasureRef.child("ownerClan").setValue(activeUser.getClan());

                            // Show ownership dialog
                            dialogOwner.setContentView(R.layout.custom_dialog_owner);
                            dialogOwner.setTitle(marker.getTitle());
                            dialogOwner.setCancelable(true);

                            // now that the dialog is set up, it's time to show it
                            dialogOwner.show();
                            collectTreasureSuccess(treasure,dialogOwner);
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                });
                AlertDialog dialog = builder.create();

                dialog.show();

            } else {
                // User is too far to collect the treasure
                Log.i("Alert", "User is too far to collect the treasure");
                Toast.makeText(this, "You are too far to collect the treasure!", Toast.LENGTH_LONG).show();

            }
        }
    }
    /* on sucess - treasure collect */

    public void collectTreasureSuccess(final Treasure treasure, final Dialog dialogOwner)
    {
        // Add experience points to user
        final DatabaseReference userAccRef = database.getReference().child("users").child(activeUser.getUsername());
        final DatabaseReference expRef = userAccRef.child("experience");
        final DatabaseReference userLevelRef = userAccRef.child("level");
        expRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long treasurePoints=(long) treasure.getPoints();
                long currentLevel=activeUser.getLevel();
                final long nextLevel=currentLevel+1;
                final DatabaseReference nextLevelRef=database.getReference().child("level").child(nextLevel+"").child("max_exp");
                nextLevelRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long currentExperience =activeUser.getExperience();
                        long maxExpOfNextLvl= (long) dataSnapshot.getValue();
                        currentExperience = currentExperience + treasurePoints;
                        if(currentExperience>=maxExpOfNextLvl)
                        {
                            long newExp= Math.abs(maxExpOfNextLvl-currentExperience);
                            expRef.setValue(newExp);
                            userLevelRef.setValue(nextLevel);
                            Toast toast=new Toast(MapsActivity.this);
                            ImageView view = new ImageView(MapsActivity.this);
                            view.setImageResource(R.drawable.levelup);
                            toast.setView(view);
                            toast.show();
                        }else
                        {
                            long newExp=Math.abs(currentExperience);
                            expRef.setValue(newExp);
                            Toast.makeText(MapsActivity.this, "Sucessfully collected", Toast.LENGTH_LONG).show();
                        }
                        // user - > treasure -- > collected = true
                        dialogOwner.setOnCancelListener(new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                showFeedbackDialog(treasure.getName());
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /* set up trivia dialog */

    private void setUpTrivia(final Treasure treasure, final Dialog triviaDialog, final Dialog challengeDialog, String treasureName, final Marker marker, final DatabaseReference userToTreasureRef, final UserToTreasure user_to_treasure, final DatabaseReference treasureRef) {
        final TextView question=triviaDialog.findViewById(R.id.questionText);
        final Button answer1=triviaDialog.findViewById(R.id.answer1);
        final Button answer2=triviaDialog.findViewById(R.id.answer2);
        final Button answer3=triviaDialog.findViewById(R.id.answer3);
        final Button answer4=triviaDialog.findViewById(R.id.answer4);

        // Find trivia question for this treasure in firebase
        DatabaseReference triviaRef=database.getReference().child("treasures_trivia").child(treasureName);
        triviaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TriviaQuestion triviaQuestion = dataSnapshot.getValue(TriviaQuestion.class);
                question.setText(triviaQuestion.getQuestion());
                answer1.setText(triviaQuestion.getAnswer1());
                answer2.setText(triviaQuestion.getAnswer2());
                answer3.setText(triviaQuestion.getAnswer3());
                answer4.setText(triviaQuestion.getAnswer4());
                final String correctAnswer=triviaQuestion.getCorrect_answer();

                     /* set up listener */
                View.OnClickListener listener =  new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button b = (Button)v;
                        String buttonText = b.getText().toString();
                        if(buttonText.equals(correctAnswer))
                        {
                            // the answer is correct
                            b.setBackgroundColor(Color.parseColor("#00722d"));
                            answer1.setEnabled(false);
                            answer2.setEnabled(false);
                            answer3.setEnabled(false);
                            answer4.setEnabled(false);

                            ImageView correctAnswer=new ImageView(MapsActivity.this);
                            correctAnswer.setImageResource(R.drawable.correctanswer);
                            //populate layout with your image and text or whatever you want to put in here
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(correctAnswer);
                            toast.show();

                            triviaDialog.cancel();
                            challengeDialog.cancel();
                            // Mark treasure as "collected" by the new owner
                            userToTreasureRef.child(activeUser.getUsername()).child(marker.getTitle()).setValue(user_to_treasure);

                            // Set the owner of the treasure as the current active user
                            treasureRef.child("owner").setValue(activeUser.getUsername());
                            treasureRef.child("ownerClan").setValue(activeUser.getClan());

                            // Show ownership dialog
                            final Dialog dialogOwner = new Dialog(MapsActivity.this);

                            dialogOwner.setContentView(R.layout.custom_dialog_owner);
                            dialogOwner.setTitle(marker.getTitle());
                            dialogOwner.setCancelable(true);

                            // now that the dialog is set up, it's time to show it
                            dialogOwner.show();
                            collectTreasureSuccess(treasure,dialogOwner);

                        }else
                        {
                            b.setBackgroundColor(Color.parseColor("#7a0c00"));
                            answer1.setEnabled(false);
                            answer2.setEnabled(false);
                            answer3.setEnabled(false);
                            answer4.setEnabled(false);

                            // wrong answer - walk the plank
                            ImageView walkThePlank=new ImageView(MapsActivity.this);
                            walkThePlank.setImageResource(R.drawable.walktheplank);
                            //populate layout with your image and text or whatever you want to put in here
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(walkThePlank);
                            toast.show();

                            final Handler handler = new Handler();

                            final Runnable r = new Runnable() {
                                public void run() {
                                    triviaDialog.cancel();
                                    challengeDialog.cancel();
                                    handler.postDelayed(this, 5000);
                                }
                            };

                            handler.postDelayed(r, 5000);
                        }

                    }
                };

                answer1.setOnClickListener(listener);
                answer2.setOnClickListener(listener);
                answer3.setOnClickListener(listener);
                answer4.setOnClickListener(listener);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /* Show dialog to leave feedback */

    private void showFeedbackDialog(final String treasureName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Feedback");
        builder.setMessage("Dear traveller, leave some feedback regarding your experience with " + treasureName);
        // Set layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // Set up the input
        final EditText input = new EditText(this);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.setHint("Your feedback");
        layout.addView(input);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set up the rating bar
        final RatingBar rating = new RatingBar(this);
        rating.setId(R.id.ratings);
        rating.setMax(5);
        rating.setNumStars(5);
        layout.addView(rating);

        ViewGroup.LayoutParams params = rating.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        rating.setLayoutParams(params);

        builder.setView(layout);
        builder.setPositiveButton("Send feedback", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             String m_Text = input.getText().toString();
             final DatabaseReference userToTreasureRef = database.getReference().child("treasures_feedback");
             userToTreasureRef.child(treasureName).child(activeUser.getUsername()).child("feedback").setValue(m_Text);
             float userRating =  rating.getRating();
             userToTreasureRef.child(treasureName).child(activeUser.getUsername()).child("rating").setValue(userRating);
             Toast.makeText(MapsActivity.this, "Feedback succesfully submitted : " + m_Text, Toast.LENGTH_LONG).show();

                mMap.clear();
             setCurrLoc();
             getNearbyTreasures();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                mMap.clear();
                setCurrLoc();
                getNearbyTreasures();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        input.clearFocus();

    }

    /* Show choose a category dialog */

    private void showCategoriesAlertDialog() {
        final Dialog dialog = new Dialog(MapsActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Filter treasures by category");

        dialog.setCancelable(true);

        // now that the dialog is set up, it's time to show it
        dialog.show();
        filterButton= (Button) dialog.findViewById(R.id.buttonfilter);
        rg = (RadioGroup) dialog.findViewById(R.id.radioGroup);
        // set up listener to filter button
        filterButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int selectedRadioButtonID = rg.getCheckedRadioButtonId();
                System.out.println(selectedRadioButtonID);
                if (selectedRadioButtonID != -1) {

                    RadioButton selectedRadioButton = (RadioButton) dialog.findViewById(selectedRadioButtonID);
                    String selectedRadioButtonText = selectedRadioButton.getText().toString();
                    dialog.hide();
                    getNearbyTreasuresByCategory(selectedRadioButtonText);
                }
                else{
                    Toast errorToast = Toast.makeText(MapsActivity.this, "Please make sure you select a category.", Toast.LENGTH_SHORT);
                    errorToast.show();
                }

            }
        });

    }

    /* Filter treasures by a specific category */

    public void getNearbyTreasuresByCategory(String category) {
        Toast filterToast = Toast.makeText(MapsActivity.this, "Filtered treasures only for category: " + category, Toast.LENGTH_LONG);
        filterToast.show();
        filterCategory=category;
        treasuresList.clear();
        mMap.clear();

        // Filter treasures by category
        final DatabaseReference treasuresRef = database.getReference().child("treasures");
        Query query=treasuresRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Treasure treasure = snapshot.getValue(Treasure.class);
                    treasuresList.add(treasure);
                }

                injectTreasuresToMap(treasuresList,true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /* helper method to set up clans */
    private void populateClanMap() {
        clanNamesImages.put("The Rogers", R.drawable.clan1);
        clanNamesImages.put("The Sparrows", R.drawable.clan2);
        clanNamesImages.put("The Blackbeards", R.drawable.clan3);
        clanNamesImages.put("The Swans", R.drawable.clan4);
        clanNamesImages.put("The Sirens", R.drawable.clan5);
        clanNamesImages.put("The Kenways", R.drawable.clan7);
    }

}