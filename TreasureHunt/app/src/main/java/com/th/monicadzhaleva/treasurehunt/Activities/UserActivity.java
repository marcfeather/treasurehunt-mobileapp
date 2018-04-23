package com.th.monicadzhaleva.treasurehunt.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.th.monicadzhaleva.treasurehunt.CustomList;
import com.th.monicadzhaleva.treasurehunt.R;
import com.th.monicadzhaleva.treasurehunt.Treasure;
import com.th.monicadzhaleva.treasurehunt.User;
import com.th.monicadzhaleva.treasurehunt.UserToTreasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    protected User activeUser;
    protected TextView usernameInfoVal;
    protected TextView firstNameInfoVal;
    protected TextView lastNameInfoVal;
    protected TextView levelInfoVal;
    protected TextView expInfoVal;
    protected ImageView avatar;
    ListView clanList;
    HashMap <String,Integer> clanNamesImages=new HashMap();

    FirebaseDatabase database;
    private List<UserToTreasure> collectedTreasureList=new ArrayList<UserToTreasure>();
    private Map<String,String> treasureInfoMap=new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        this.setTitle("User profile");
        populateClanMap();
        database = FirebaseDatabase.getInstance();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        getUserDetails();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final String receiver=getIntent().getExtras().get("currentActiveUser").toString();

        FloatingActionButton sendmessage = (FloatingActionButton) findViewById(R.id.sendmessage);
        if(receiver.equals(activeUser.getUsername()))
        {
            /* Hide button if the user is both sender and receiver - >
            cannot message your own account
             */
            sendmessage.hide();
            sendmessage.setAlpha(0);
            ((ViewGroup) sendmessage.getParent()).removeView(sendmessage);

        }
        sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivity = new Intent(UserActivity.this, ChatActivity.class);
                chatActivity.putExtra("activeUser", receiver); // the user receiving the message
                chatActivity.putExtra("sender",activeUser.getUsername()); // the user sending the message
                startActivity(chatActivity);
            }
        });
    }


    private void populateClanMap() {
        clanNamesImages.put("The Rogers", R.drawable.clan1);
        clanNamesImages.put("The Sparrows", R.drawable.clan2);
        clanNamesImages.put("The Blackbeards", R.drawable.clan3);
        clanNamesImages.put("The Swans", R.drawable.clan4);
        clanNamesImages.put("The Sirens", R.drawable.clan5);
        clanNamesImages.put("The Kenways", R.drawable.clan7);
    }

    public void getUserDetails()
    {
        // Get user details from login intent screen
        activeUser=new User();
        final DatabaseReference activeUserRef=database.getReference().child("users").child(getIntent().getStringExtra("username"));
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeUser= dataSnapshot.getValue(User.class);
                setUserInfo(activeUser);
                getCollectedTreasures(activeUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        activeUserRef.addValueEventListener(postListener);
    }

    protected void setUserInfo(User user)
    {
        usernameInfoVal= (TextView) findViewById(R.id.usernameinfovalue);
        firstNameInfoVal= (TextView) findViewById(R.id.firstNameinfovalue);
        lastNameInfoVal= (TextView) findViewById(R.id.lastNameinfovalue);
        levelInfoVal= (TextView) findViewById(R.id.levelinfovalue);
        expInfoVal= (TextView) findViewById(R.id.experienceinfovalue);
        avatar= (ImageView) findViewById(R.id.avatarView);
        clanList= (ListView) findViewById(R.id.individualClanList);

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

        try {
            this.setTitle(user.getUsername());
            usernameInfoVal.setText(user.getUsername());
            firstNameInfoVal.setText(user.getFirstName());
            lastNameInfoVal.setText(user.getLastName());
            levelInfoVal.setText(String.valueOf(user.getLevel()));
            expInfoVal.setText(String.valueOf(user.getExperience()));
            String userclan=user.getClan();
            String[]userClans={userclan};
            int userClanImage=clanNamesImages.get(userclan);
            Integer[]userImages={userClanImage};
            CustomList adapter = new
                    CustomList(UserActivity.this, userClans, userImages);
            clanList.setAdapter(adapter);

        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void getCollectedTreasures(User user)
    {
        final DatabaseReference userToTreasureRef=database.getReference().child("user_to_treasure");
        final DatabaseReference userToTreasureInstanceRef =  userToTreasureRef.child(user.getUsername());
        userToTreasureInstanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserToTreasure userToTreasure = snapshot.getValue(UserToTreasure.class);
                    userToTreasure.setTreasureName(snapshot.getKey());
                    collectedTreasureList.add(userToTreasure);
                }
                getCollectedTreasuresInfo(collectedTreasureList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getCollectedTreasuresInfo(final List<UserToTreasure> collectedTreasureList) {
        for (UserToTreasure userToTreasure : collectedTreasureList) {
            String treasureName = userToTreasure.getTreasureName();
            final DatabaseReference treasureRef = database.getReference().child("treasures").child(treasureName);
            treasureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Treasure treasure=dataSnapshot.getValue(Treasure.class);
                    if(treasure!=null) {
                        treasureInfoMap.put(treasure.getName(), treasure.getInfo());
                        addCollectedTreasures(collectedTreasureList);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void addCollectedTreasures(List<UserToTreasure> collectedTreasureList)
    {
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.collectedTreasuresContainer);
        int prevTextViewId = 0;

        for (UserToTreasure userToTreasure : collectedTreasureList) {
            String treasureName = userToTreasure.getTreasureName();

            final TextView textView = new TextView(this);
            if(treasureInfoMap.get(treasureName)!=null) {
                textView.setText(treasureName + " : " + treasureInfoMap.get(treasureName));
            }else
            {
                textView.setText(treasureName);
            }
            textView.setTextColor(Color.WHITE);

            int curTextViewId = prevTextViewId + 1;
            textView.setId(curTextViewId);
            final RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.BELOW, prevTextViewId);
            params.bottomMargin=10;
            textView.setLayoutParams(params);

            prevTextViewId = curTextViewId;
            layout.addView(textView, params);

        }
    }

}