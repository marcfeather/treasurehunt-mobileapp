package com.th.monicadzhaleva.treasurehunt.Activities;

import android.app.Dialog;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.th.monicadzhaleva.treasurehunt.CustomList;
import com.th.monicadzhaleva.treasurehunt.R;
import com.th.monicadzhaleva.treasurehunt.Treasure;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;


public class RecommendationsActivity extends AppCompatActivity {

    private String activeUserUsername;
    private String recommendedTreasures;
    FirebaseDatabase database;
    public HashMap<String,Integer> clanNamesImages=new HashMap<>();
    int prevId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);
        activeUserUsername = getIntent().getExtras().get("username").toString();
        database = FirebaseDatabase.getInstance();
        populateClanMap();
        callRecommenderApi();
    }

    /**
     * Calls the treasure hunt recommendations servlet with a parameter - the user's username
     */
    private void callRecommenderApi() {
        final String urlString = "https://alpha-001-dot-treasurehunt-3540e.appspot.com/recommend?username="+activeUserUsername;
        new Thread(new Runnable() {
            public void run() {

                try {
                    URL url = new URL(urlString);

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        recommendedTreasures=readStream(in,10000000);
                        showRecommendations();
                    } finally {
                        urlConnection.disconnect();
                    }


                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                }

            }
        }).start();

    }

    /**
     * Gets the array of recommended treasures from the servlet response
     */
    private void showRecommendations() {
        String[] recommendedTreasuresArr = recommendedTreasures.split(",");
        for (String treasureName : recommendedTreasuresArr) {
            int count=1;
            final DatabaseReference treasureRef = database.getReference().child("treasures").child(treasureName);
            treasureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Treasure treasure = dataSnapshot.getValue(Treasure.class);
                    addTreasureToRecommendActivity(treasure);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Shows a treasure as a "recommendation" to user
     */
    private void addTreasureToRecommendActivity(final Treasure treasure) {
        final String treasureName=treasure.getName();
        final String treasureInfo=treasure.getInfo();
        final String treasureUrl=treasure.getImageUrl();
        String treasureCategory=treasure.getCategory();
        Random r = new Random();
        int randomNo = r.nextInt(1000 + 1);

        RelativeLayout recommenderContainer = (RelativeLayout) findViewById(R.id.recommendationsContainer);

        LinearLayout recommendationLayout = new LinearLayout(this);
        recommendationLayout.setOrientation(LinearLayout.VERTICAL);
        recommendationLayout.setId(randomNo);
        recommendationLayout.setPadding(20,20,20,20);
        recommendationLayout.setBackgroundColor(Color.parseColor("#3f3f3f"));
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.setMargins(0, 0, 0, 20);
        if(prevId==0) {
            relativeParams.addRule(RelativeLayout.BELOW, R.id.textViewtop);
        }else{
            relativeParams.addRule(RelativeLayout.BELOW, prevId);
        }
        recommendationLayout.setLayoutParams(relativeParams);

        /* add treasure name to container */
        TextView treasureNameView=new TextView(RecommendationsActivity.this);
        treasureNameView.setTextColor(Color.parseColor("#eeff07"));
        treasureNameView.setText(treasureName);
        recommendationLayout.addView(treasureNameView);

        /* add treasure category to container */
        TextView treasureCategoryView=new TextView(RecommendationsActivity.this);
        treasureCategoryView.setTextColor(Color.parseColor("#4cd5ff"));
        treasureCategoryView.setText(treasureCategory);
        recommendationLayout.addView(treasureCategoryView);

        /* add treasure info to container */
        TextView treasureInfoView=new TextView(RecommendationsActivity.this);
        treasureInfoView.setTextColor(Color.parseColor("#ffffff"));
        treasureInfoView.setText(treasureInfo);
        recommendationLayout.addView(treasureInfoView);

        recommendationLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){

                 /* Show custom dialog about location */
                    final Dialog dialog = new Dialog(RecommendationsActivity.this);
                    dialog.setContentView(R.layout.custom_dialog_treasure);
                    dialog.setTitle(treasureName);

                    dialog.setCancelable(true);


                    // now that the dialog is set up, it's time to show it
                    dialog.show();
                    Button collectButton = (Button) dialog.findViewById(R.id.buttonCollect);
                    collectButton.setVisibility(View.INVISIBLE);
                    TextView treasureOwnerInfo = (TextView) dialog.findViewById(R.id.treasureOwnerInfoText);
                    if(treasure.getOwner()==null)
                    {
                        treasureOwnerInfo.setText("No one owns this treasure. Be the first one to collect it!");
                    }else {
                        treasureOwnerInfo.setText(treasure.getOwner());
                    }
                    ListView treasureOwnerClanList = (ListView) dialog.findViewById(R.id.treasureClanInfoText);
                    if(treasure.getOwnerClan()!=null) {
                        String userclan = treasure.getOwnerClan();
                        String[] userClans = {userclan};
                        if(clanNamesImages.get(userclan)!=null) {
                            int userClanImage = clanNamesImages.get(userclan);
                            Integer[] userImages = {userClanImage};
                            CustomList adapter = new
                                    CustomList(RecommendationsActivity.this, userClans, userImages);
                            treasureOwnerClanList.setAdapter(adapter);
                        }
                    }else{
                        TextView treasureOwnerClan = (TextView) dialog.findViewById(R.id.treasureClanInfo);
                        treasureOwnerClan.setVisibility(View.INVISIBLE);
                    }
                    ImageView treasureImage = (ImageView) dialog.findViewById(R.id.imageViewTreasure);

                        TextView treasureNameDialog = (TextView) dialog.findViewById(R.id.treasureName);
                        treasureNameDialog.setText(treasureName);
                        TextView treasureInfoDialog = (TextView) dialog.findViewById(R.id.treasureInfo);
                        treasureInfoDialog.setText(treasureInfo);
                        if(treasureUrl!=null)
                        {
                            Picasso.get().load(treasureUrl).into(treasureImage);
                        }
                    }
                    return true;
                }
            });

        recommenderContainer.addView(recommendationLayout);
        prevId=randomNo;
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    public String readStream(InputStream stream, int maxReadSize) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
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
