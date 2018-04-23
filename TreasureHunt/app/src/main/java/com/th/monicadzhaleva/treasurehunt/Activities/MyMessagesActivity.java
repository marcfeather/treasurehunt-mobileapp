package com.th.monicadzhaleva.treasurehunt.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.th.monicadzhaleva.treasurehunt.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MyMessagesActivity extends AppCompatActivity {

    ListView listView;
    public FirebaseDatabase database;
    private ArrayList<String> usersInInbox;
    final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    private String activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);
        listView= (ListView) findViewById(R.id.listOfMessages);
        TextView emptyText = (TextView)findViewById(R.id.emptyView);
        listView.setEmptyView(emptyText);
        database = FirebaseDatabase.getInstance();
        usersInInbox=new ArrayList<String>();
        activeUser=getIntent().getStringExtra("username");
        getMyMessages();

    }

    public void getMyMessages() {
        getMessagesSentByMe();
    }

    private void populateListView() {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                list,
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
                String playerName = c.getText().toString();

                // Open the selected user profile
                Intent chatActivity = new Intent(MyMessagesActivity.this, ChatActivity.class);
                chatActivity.putExtra("activeUser", activeUser);
                chatActivity.putExtra("sender", playerName);
                startActivity(chatActivity);

            }
        });
    }

    public void getMessagesSentByMe() {
        final DatabaseReference userMessages = database.getReference().child("chat").child(activeUser);
        userMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> temp = new HashMap<String, String>();
                        String sender = snapshot.getKey();
                        temp.put("title", sender);
                        temp.put("description", "Tap to view messages with " + sender + "...");
                        if(!list.contains(temp)) {
                            list.add(temp);
                        }
                    }
                    getMessagesSentToMe();
                }else{
                    getMessagesSentToMe();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void getMessagesSentToMe() {
        final DatabaseReference userMessages = database.getReference().child("chat");
        userMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    String sender=child.getKey();
                    if(!sender.equals(activeUser)) {
                        // if the active user is not the sender
                        for (DataSnapshot child2 : child.getChildren())
                        {
                            if(child2.getKey().equals(activeUser))
                            {
                                // but the active user is a receiver
                                HashMap<String, String> temp = new HashMap<String, String>();
                                temp.put("title", sender);
                                temp.put("description", "Tap to view messages with " + sender + "...");
                                if(!list.contains(temp)) {
                                    list.add(temp);
                                }
                            }
                        }
                    }

                }
                if(list.size()>1) {
                    populateListView();
                }else{
                    TextView view=new TextView(MyMessagesActivity.this);
                    view.setText("No messages to display...");
                    view.setTextColor(Color.WHITE);
                    listView.setEmptyView(view);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}
