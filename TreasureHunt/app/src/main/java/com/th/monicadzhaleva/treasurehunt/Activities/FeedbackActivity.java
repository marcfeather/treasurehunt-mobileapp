package com.th.monicadzhaleva.treasurehunt.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.th.monicadzhaleva.treasurehunt.R;
import com.th.monicadzhaleva.treasurehunt.Treasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FeedbackActivity extends AppCompatActivity {
    LinearLayout firstLayout,thirdLayout;
    public FirebaseDatabase database;
    HashMap<String, String> treasureMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        database = FirebaseDatabase.getInstance();
        firstLayout= (LinearLayout) findViewById(R.id.firstLayout);
        thirdLayout= (LinearLayout) findViewById(R.id.thirdLayout);
        Intent intent = getIntent();
        treasureMap = (HashMap<String, String>)intent.getSerializableExtra("map");

        // display treasures in the left panel
        getTreasuresList();
        }

    private void addTreasures(ArrayList<String> treasures) {
        int prevTextViewId = 0;
        for (String treasureName : treasures) {
            final TextView textView = new TextView(this);
            if (treasureName != null&&!treasureName.equals("")) {
                textView.setText(treasureName);

            textView.setTextColor(Color.WHITE);
            textView.setTextSize(15);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Displaying feedback: ",textView.getText().toString());
                    String treasureName=textView.getText().toString();
                    displayFeedback(treasureName);
                }
            });
            int curTextViewId = prevTextViewId + 1;
            if(isEven(curTextViewId))
            {
                textView.setBackgroundColor(Color.parseColor("#595959"));
            }

            textView.setId(curTextViewId);
            final RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.BELOW, prevTextViewId);
            params.bottomMargin = 30;
            params.topMargin = 20;

            textView.setPadding(0,15,0,15);
            textView.setLayoutParams(params);

            prevTextViewId = curTextViewId;
            firstLayout.addView(textView, params);
            }
        }
    }

    private void displayFeedback(final String treasureName) {
        // remove Previous views
        thirdLayout.removeAllViews();

        // get treasure feedback
        final DatabaseReference treasureFeedback = database.getReference().child("treasures_feedback").child(treasureName);
        treasureFeedback.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    int count=0;
                    LinearLayout picLL = new LinearLayout(FeedbackActivity.this);
                    picLL.setOrientation(LinearLayout.VERTICAL);
                    ImageView treasureImg=new ImageView(FeedbackActivity.this);
                    Random r = new Random();
                    int randomNo = r.nextInt(1000+1);
                    treasureImg.setId(randomNo);
                    String imageUrl=treasureMap.get(treasureName);
                    Picasso.get().load(imageUrl).into(treasureImg);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
                    treasureImg.setLayoutParams(layoutParams);

                   final TextView feedbackView = new TextView(FeedbackActivity.this);
                    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params1.addRule(RelativeLayout.BELOW, treasureImg.getId());
                    feedbackView.setLayoutParams(params1);
                    feedbackView.setText("Displaying feedback for: " + treasureName);
                    feedbackView.setTextColor(Color.YELLOW);
                    feedbackView.setPadding(0,0,0,10);

                    final RelativeLayout.LayoutParams params=
                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ABOVE, count);

                    picLL.addView(treasureImg);
                    picLL.addView(feedbackView);

                    thirdLayout.addView(picLL, params);



                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                       String user=snapshot.getKey();
                       String feedback=snapshot.getValue().toString();
                       displaySingleFeedback(user,feedback,count);
                       count++;
                    }
                }else{
                   // No feedback for given treasure
                    final TextView noFeedbackView = new TextView(FeedbackActivity.this);
                    noFeedbackView.setText("No feedback to display.");
                    noFeedbackView.setTextColor(Color.YELLOW);
                    final RelativeLayout.LayoutParams params=
                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);

                    thirdLayout.addView(noFeedbackView, params);

                }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }

    private void displaySingleFeedback(String user, String feedback, int curTextViewId) {
        final TextView textViewUser = new TextView(this);
        textViewUser.setText(user);
        textViewUser.setTextColor(Color.YELLOW);
        textViewUser.setTextSize(15);
        String[] feedbackRatingText=feedback.split(",");
        String feedbackRating="";
        String feedbackText="";
        int rating=-1;

        if (feedbackRatingText.length==2)
        {
            feedbackRating=feedbackRatingText[0].replace("{","").replace("}","").replace("rating=","");
            feedbackText=feedbackRatingText[1].replace("{","").replace("}","").replace("feedback=","");
            rating=Integer.parseInt(feedbackRating);

        }else
        {
            feedbackText=feedbackRatingText[0].replace("{","").replace("}","").replace("feedback=","");
        }

        final TextView textViewFeedback = new TextView(this);
        textViewFeedback.setText(feedbackText);
        textViewFeedback.setTextColor(Color.WHITE);

        RelativeLayout layoutRatingView=new RelativeLayout(FeedbackActivity.this);

        if(isEven(curTextViewId))
        {
            textViewUser.setBackgroundColor(Color.parseColor("#595959"));
            textViewFeedback.setBackgroundColor(Color.parseColor("#595959"));
            layoutRatingView.setBackgroundColor(Color.parseColor("#595959"));
        }
        textViewUser.setId(curTextViewId-2);
        textViewFeedback.setId(curTextViewId);

        final RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, curTextViewId);
        params.topMargin = 20;
        final RelativeLayout.LayoutParams  params2=
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.BELOW, curTextViewId-2);
        params2.bottomMargin=20;
        textViewUser.setPadding(0,15,0,0);
        textViewUser.setLayoutParams(params);
        textViewFeedback.setPadding(0,5,0,15);
        textViewFeedback.setLayoutParams(params2);

        // Display username & feedback

        thirdLayout.addView(textViewUser, params);
        thirdLayout.addView(textViewFeedback, params2);

        // Display Rating
        if(rating!=-1) {
            final RatingBar ratingView = new RatingBar(this);
            ratingView.setMax(5);
            ratingView.setNumStars(rating);
            ratingView.setIsIndicator(true);
            ratingView.setFocusable(false);
            Drawable drawable = ratingView.getProgressDrawable();
            drawable.setColorFilter(Color.parseColor("#0064A8"),PorterDuff.Mode.SRC_ATOP);

            final RelativeLayout.LayoutParams  params3=
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            params3.addRule(RelativeLayout.ABOVE,curTextViewId);
            params3.addRule(RelativeLayout.BELOW,curTextViewId-2);
            params3.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutRatingView.addView(ratingView,params3);
            thirdLayout.addView(layoutRatingView);

        }
    }

    public void getTreasuresList() {
        database = FirebaseDatabase.getInstance();
        final DatabaseReference treasuresRef = database.getReference().child("treasures");
        treasuresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> treasuresList = new ArrayList<String>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Treasure treasure = snapshot.getValue(Treasure.class);
                    treasuresList.add(treasure.getName());
                }
                addTreasures((ArrayList<String>) treasuresList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isEven(int x)
    {
        if ( x % 2 == 0 )
            return true;
        else
            return false;
    }
}
