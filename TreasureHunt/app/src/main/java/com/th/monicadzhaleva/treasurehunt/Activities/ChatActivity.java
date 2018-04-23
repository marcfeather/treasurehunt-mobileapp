package com.th.monicadzhaleva.treasurehunt.Activities;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.th.monicadzhaleva.treasurehunt.Objects.Message;
import com.th.monicadzhaleva.treasurehunt.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private String activeUser;
    private String sender;
    public FirebaseDatabase database;
    private ArrayList<Message> messagesList;
    private ImageView imageView;
    private ScrollView scrollView;
    private EditText editText;
    TextView chatWith;
    RelativeLayout chatWindow; // the outer frame
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        database = FirebaseDatabase.getInstance();

        /* get current sender and receiver */
        activeUser=getIntent().getExtras().get("activeUser").toString(); // the user receiving the message
        sender= getIntent().getExtras().get("sender").toString(); // the user sending it (i.e. the current user of the application)
        messagesList=new ArrayList<>();

        /* get xml elements */
        chatWindow= (RelativeLayout) findViewById(R.id.chatWindow) ;
        imageView= (ImageView) findViewById(R.id.sendButton);
        chatWith= (TextView) findViewById(R.id.textView2);
        editText= (EditText) findViewById(R.id.messageText);
        scrollView= (ScrollView) findViewById(R.id.scrollview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v) {
                sendMessage();
            }
        });
        displayChatParticipant();
        getChat();

    }

    private void displayChatParticipant() {
        chatWith.setText("Conversation with " + sender + " ...");
    }

    /* This method gets chat messages from receiver */
    public void getChat()
    {
        final DatabaseReference userMessages = database.getReference().child("chat").child(activeUser).child(sender);
        userMessages.addListenerForSingleValueEvent(new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue()!=null) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                       Message message=snapshot.getValue(Message.class);
                        messagesList.add(message);
                }
                getChatBySender();
            }else{
              // Could not retrieve messages
                getChatBySender();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    });
    }

    /* This method gets chat messages from Sender*/
    public void getChatBySender()
    {
        final DatabaseReference userMessages = database.getReference().child("chat");
        userMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String senderOfMessage = child.getKey();
                    if (!senderOfMessage.equals(activeUser)) {
                        if(senderOfMessage.equals(sender)) {
                            for (DataSnapshot child2 : child.getChildren()) {
                                if (child2.getKey().equals(activeUser)) {
                                    for (DataSnapshot snapshot : child2.getChildren()) {
                                        Message message = snapshot.getValue(Message.class);
                                        messagesList.add(message);
                                    }
                                }
                            }
                        }
                    }
                }
                displayMessagesInView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    /* This method adds the chat messages to the view */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayMessagesInView()
    {
        chatWindow.removeAllViews();
        count=1;
        for(Message message: messagesList)
        {
            addMessage(message);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addMessage(Message message)
    {
        /*
         Create a linear layout
          */
        LinearLayout messageBox=new LinearLayout(this);
        messageBox.setId(count);
        messageBox.setOrientation(LinearLayout.HORIZONTAL);
        // Create params with rules
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, count-1);
        params.setMargins(0,30,0,0);


        /* Create a text
        view
         */
        TextView messageText=new TextView(this);
        if(sender.equals(message.getMessageUser())) {
            // if active user is also the sender of the message
            messageText.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner1));

        }else
        {
            messageText.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner));
        }

        messageText.setPadding(20,5,20,12);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if(sender.equals(message.getMessageUser())) {
                messageText.setText(Html.fromHtml("<font color='yellow'><b>" + message.getMessageUser() + "</b></font>:  " + message.getMessageText() + " <font color='#a8a8a8' align=right><small><i>"+df2.format(new Date(message.getMessageTime()))+"</small></i></font>", Html.FROM_HTML_MODE_LEGACY));
            }else{
                messageText.setText(Html.fromHtml("<font color='#66b3ff'><b>" + message.getMessageUser() + "</b></font>:  " + message.getMessageText() + "<font color='#a8a8a8' align=right><small><i>"+df2.format(new Date(message.getMessageTime()))+"</small></i></font>", Html.FROM_HTML_MODE_LEGACY));
            }
        }else
        {
            if(sender.equals(message.getMessageUser())) {
                messageText.setText(Html.fromHtml("<font color='yellow'><b>" + message.getMessageUser() + "</b></font>:  " + message.getMessageText() +" <font color='#a8a8a8' align=right><small><i>"+df2.format(new Date(message.getMessageTime()))+"</small></i></font>"));
            }else{
                messageText.setText(Html.fromHtml("<font color='#66b3ff'><b>" + message.getMessageUser() + "</b></font>:  " + message.getMessageText() +  " <font color='#a8a8a8' align=right><small><i>"+df2.format(new Date(message.getMessageTime()))+"</small></i></font>"));
            }
        }
        messageText.setId(count);
        messageText.setTextColor(Color.WHITE);


        // Create params with rules
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        paramsText.setMargins(0,10,0,10);
        /* Create image
         view
          */
        ImageView avatar=new ImageView(this);
        // Create params with rules
        RelativeLayout.LayoutParams paramsImg = new RelativeLayout.LayoutParams(40,40);
        paramsImg.width=40;
        paramsImg.height=40;
        paramsImg.topMargin=0;
        if(sender.equals(message.getMessageUser())) {
            avatar.setBackgroundResource(R.drawable.avatar1);
            paramsText.width=480;
            // Add text view to linear layout
            messageBox.addView(messageText,paramsText);
            // Add image view to linear layout
            messageBox.addView(avatar,paramsImg);

        }else
        {
            // if active user is also the sender of the message
            avatar.setBackgroundResource(R.drawable.avatar2 );
            // Add image view to linear layout
            messageBox.addView(avatar,paramsImg);
            // Add text view to linear layout
            messageBox.addView(messageText,paramsText);
    }


        // Add to outer container
        chatWindow.addView(messageBox, params);
        messageBox.requestFocus();
        // increment count
        count=count+1;
        scrollView.post(new Runnable() {
            @Override
            public void run(){
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    /* This method allows the user to send a message in the chat */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendMessage() {
        String messageText=editText.getText().toString();
        if(messageText!=null&&!messageText.equals("")) {
            Message newMessage = new Message();
            newMessage.setMessageText(messageText);
            newMessage.setMessageUser(activeUser);
            newMessage.setMessageTime(new Date().getTime());
            DatabaseReference chatRef = database.getReference("chat").child(activeUser).child(sender); // the user receiving the message - > the user sending it
            String key = chatRef.push().getKey();
            chatRef.child(key).setValue(newMessage);
            editText.getText().clear();
            addMessage(newMessage);
        }else
        {
            Toast newToast= Toast.makeText(this,"Please input a message...",Toast.LENGTH_SHORT);
        }


    }

}
