package com.th.monicadzhaleva.treasurehunt.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.th.monicadzhaleva.treasurehunt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    ListView listView;
    public FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        listView= (ListView) findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();

        getTopUsersByTreasures();
    }

    public ArrayList getTopUsersByTreasures() {
        // get all users that have collected treasures
        final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();

        final DatabaseReference usersToTreasures = database.getReference().child("user_to_treasure");
        usersToTreasures.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    Map<String, Object>  users = (Map<String, Object>) dataSnapshot.getValue();
                    Iterator it = users.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                       String userName= (String) pair.getKey();
                       HashMap<String,Object> value= (HashMap<String, Object>) pair.getValue();
                       int numberOfTreasures= value.size();
                        HashMap<String,String> temp = new HashMap<String,String>();
                        temp.put("title",userName);
                        temp.put("description", numberOfTreasures+"");
                        list.add(temp);
                    }
                    // sort list by the number of collected treasures

                    final ArrayList<HashMap<String,String>> sortedList =sortListByNumTreasures(list);
                    populateListView(sortedList);
                }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return list;
    }

    /* Sort the list of users and treasures by number of collected treasures */

    private ArrayList<HashMap<String,String>>  sortListByNumTreasures(ArrayList<HashMap<String, String>> list) {
        HashMap<String, String> temporary;

        // Sort the treasures
        for (int c = 0; c < (list.size() - 1); c++) {
            for (int d = 0; d < (list.size() - c - 1); d++) {

                if (Integer.parseInt(list.get(d).get("description")) < Integer
                        .parseInt(list.get(d + 1).get("description"))) {

                    temporary = list.get(d);
                    list.set(d,list.get(d + 1));
                    list.set(d + 1, temporary);
                }
            }
        }

        // Set up new sorted list
        for (int i=0; i< (list.size());i++)
        {
                HashMap<String,String> newMap=new HashMap<>();
                newMap.put("title", list.get(i).get("title"));
                newMap.put("description", "Collected treasures: " + list.get(i).get("description"));
                list.set(i,newMap);
        }



        return list;
    }

    public void populateListView(ArrayList topUsers)
    {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                topUsers,
                R.layout.custom_list,
                new String[] {"title","description"},
                new int[] {R.id.title,R.id.desc,}

        );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
        {
            TextView c = (TextView) arg1.findViewById(R.id.title);
            String playerName = c.getText().toString(); // the user who's profile we will view
            String currentActiveUser=getIntent().getExtras().get("username").toString(); // the current active user playing the game
            // Open the selected user profile
            Intent userprofileactivity = new Intent(LeaderboardActivity.this, UserActivity.class);
            userprofileactivity.putExtra("username", playerName); //
            userprofileactivity.putExtra("currentActiveUser", currentActiveUser);

            startActivity(userprofileactivity);

        }
    });
    }
}

