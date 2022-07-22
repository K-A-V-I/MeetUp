package com.codewithbee.meetup.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithbee.meetup.MainActivity;
import com.codewithbee.meetup.Matches.MatchesActivity;
import com.codewithbee.meetup.R;
import com.codewithbee.meetup.SendNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private EditText mSendEditText;
    private ImageButton mBack;

    private ImageButton mSendButton;
    private String notification;
    private String currentUserId, matchId, chatId;
    private String matchName, matchGive, matchNeed,matchBudget, matchProfile;
    private String lastMessage,lastTimeStamp;
    private String message, createdByUser, isSeen, messageId, currentUserName;
    private Boolean currentUserBoolean;
    ValueEventListener seenListener;
    DatabaseReference mDatabaseUser,mDatabaseChat;



    protected void onCreate(Bundle savedInstanceState, boolean bottom, boolean oldBottom) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchId = getIntent().getExtras().getString("matchId");
        matchName = getIntent().getExtras().getString("matchName");
        matchGive = getIntent().getExtras().getString("give");
        matchNeed = getIntent().getExtras().getString("need");
        matchBudget = getIntent().getExtras().getString("profile");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUserId).child("connections").child("matches").child(matchId).child("ChatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");

        getChatId();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setFocusable(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mSendEditText = findViewById(R.id.message);
        mBack = findViewById(R.id.chatBack);

        mSendButton = findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (bottom && oldBottom) {
                  mRecyclerView.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount()-1);
                      }
                  },100);
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        Map onchat = new HashMap();
        onchat.put("onchat",matchId);
        reference.updateChildren(onchat);

        DatabaseReference current = FirebaseDatabase.getInstance().getReference("users")
                .child(matchId).child("connections").child("matches").child(currentUserId);
        Map lastSeen = new HashMap();
        lastSeen.put("lastSeen", "false");
        current.updateChildren(lastSeen);
    }

    @Override
    protected void onPause() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        Map onchat = new HashMap();
        onchat.put("onchat","None");
        reference.updateChildren(onchat);
        super.onPause();
    }

    @Override
    protected void onStop() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        Map onchat = new HashMap();
        onchat.put("onchat","None");
        reference.updateChildren(onchat);
        super.onStop();
    }

    private void seenMessage(final String text) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(matchId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("onchat").exists()) {
                        if (dataSnapshot.child("notificationKey").exists())
                            notification = dataSnapshot.child("notificationKey").getValue().toString();
                        else
                            notification = "";

                        if (!dataSnapshot.child("onchat").getValue().toString().equals(currentUserId)) {
                            new SendNotification(text, "New message from:" + currentUserName,notification, "activityToBeOpened", "MatchesActivity");
                        }
                        else {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(currentUserId).child("connections").child("matches").child(matchId);
                            Map seenInfo = new HashMap();
                            seenInfo.put("lastSend","false");
                            reference.updateChildren(seenInfo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu,menu);
        TextView mMatchNameTextView = (TextView) findViewById(R.id.chatToolbar);
        mMatchNameTextView.setText(matchName);
        return true;
    }

    public void showProfile(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.item_profile, null);

        TextView name = (TextView) popupView.findViewById(R.id.name);
        ImageView image = (ImageView) findViewById(R.id.image);
        TextView budget = (TextView) findViewById(R.id.budget);
        ImageView mNeedImage = (ImageView) findViewById(R.id.needImage);
        ImageView mGiveImage = (ImageView) findViewById(R.id.giveImage);

        name.setText(matchName);
        budget.setText(matchBudget);


        //need image
        if (matchNeed.equals("Netflix"))
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.netflix));
        else if (matchNeed.equals("Hulu"))
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.hulu));
        else if (matchNeed.equals("Amazon Prime"))
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.amazon));
        else if (matchNeed.equals("HBO Now"))
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.hbo));
        else if (matchNeed.equals("YouTube Originals"))
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.youtube));
        else
            mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.none));

        //give image
        if (matchNeed.equals("Netflix"))
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.netflix));
        else if (matchNeed.equals("Hulu"))
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.hulu));
        else if (matchNeed.equals("Amazon Prime"))
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.amazon));
        else if (matchNeed.equals("HBO Now"))
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.hbo));
        else if (matchNeed.equals("YouTube Originals"))
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.youtube));
        else
            mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.none));

        switch (matchProfile) {
            case "default" :
                Glide.with(popupView.getContext()).load(R.drawable.profile).into(image);
                break;

            default:
                Glide.clear(image);
                Glide.with(popupView.getContext()).load(matchProfile).into(image);
                break;
        }
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        hideSoftKeyBoard();

        popupWindow.showAtLocation(v, Gravity.CENTER,0,0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
               popupWindow.dismiss();
               return true;
            }
        });
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.unmatch) {
            new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("unmatch")
                    .setMessage("Are you sure you want to unmatch?")
                    .setPositiveButton("unmatch", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMatch(matchId);
                            Intent intent =  new Intent(ChatActivity.this, MatchesActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(ChatActivity.this, "unmatch Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Dismiss", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else if (item.getItemId() == R.id.viewProfile) {
            showProfile(findViewById(R.id.content));
        }
        return  super.onOptionsItemSelected(item);
    }

    private void deleteMatch(String matchId) {

        DatabaseReference matchId_in_UserId_dbReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUserId).child("Connections").child("matches").child(matchId);
        DatabaseReference userId_in_matchId_dbReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(matchId).child("Connections").child("matches").child(currentUserId);
        DatabaseReference yeps_in_matchId_dbReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(matchId).child("Connections").child("yeps").child(currentUserId);
        DatabaseReference yeps_in_UserId_dbReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUserId).child("Connections").child("yeps").child(matchId);

        DatabaseReference matchId_chat_dbReference = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);

        matchId_chat_dbReference.removeValue();
        matchId_in_UserId_dbReference.removeValue();
        userId_in_matchId_dbReference.removeValue();
        yeps_in_matchId_dbReference.removeValue();

    }

    private void sendMessage() {
        final String sendMessageText = mSendEditText.getText().toString();
        long now = System.currentTimeMillis();
        String timeStamp = Long.toString(now);

        if(!sendMessageText.isEmpty()) {
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserId);
            newMessage.put("text", sendMessageText);
            newMessage.put("timeStamp",timeStamp);
            newMessage.put("seen","false");

            DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("name").exists())
                            currentUserName = dataSnapshot.child("name").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

           lastMessage = sendMessageText;
           lastTimeStamp = timeStamp;
           updateLastMessage();
           seenMessage(sendMessageText);
           newMessageDb.setValue(newMessage);

        }
        mSendEditText.setText(null);
    }

    private void updateLastMessage() {
        DatabaseReference currentUserDb =FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId)
                .child("connections").child("matches").child(matchId);
        DatabaseReference matchDb =FirebaseDatabase.getInstance().getReference().child("Users").child(matchId)
                .child("connections").child("matches").child(currentUserId);

        Map lastMessageMap = new HashMap();
        lastMessageMap.put("lastMessage", lastMessage);
        Map lastTimeStampMap = new HashMap();
        lastTimeStampMap.put("lastTImeStamp",lastTimeStampMap);

        Map lastSeen = new HashMap();
        lastSeen.put("lastSeen","true");
        currentUserDb.updateChildren(lastSeen);
        currentUserDb.updateChildren(lastSeen);
        currentUserDb.updateChildren(lastMessageMap);
        currentUserDb.updateChildren(lastTimeStampMap);

        matchDb.updateChildren(lastMessageMap);
        matchDb.updateChildren(lastTimeStampMap);



    }



    private void getChatId() {
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                   chatId = dataSnapshot.getValue().toString();
                   mDatabaseChat = mDatabaseChat.child(chatId);
                   getChatMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    messageId = null;
                    message = null;
                    createdByUser = null;
                    isSeen = null;
                    if (dataSnapshot.child("text").getValue() != null){
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if (dataSnapshot.child("createdByUser").getValue() != null){
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }
                    if (dataSnapshot.child("seen").getValue() != null){
                        isSeen = dataSnapshot.child("seen").getValue().toString();
                    }
                    else isSeen = "true";

                    messageId = dataSnapshot.getKey().toString();
                    if (message != null && createdByUser != null) {
                        currentUserBoolean = false;
                        if (createdByUser.equals(currentUserId)){
                            currentUserBoolean = true;
                        }
                        ChatObject newMessage = null;
                        if (isSeen.equals("false")) {
                            if (!currentUserBoolean){
                                isSeen = "true" ;

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat")
                                        .child(chatId).child(messageId);
                                Map seenInfo = new HashMap();
                                seenInfo.put("seen","true");
                                reference.updateChildren(seenInfo);

                                newMessage = new ChatObject(message,currentUserBoolean,true);
                            }
                            else {
                                newMessage = new ChatObject(message,currentUserBoolean,false);
                            }
                        }
                        else {
                            newMessage = new ChatObject(message,currentUserBoolean,true);
                        }
                        DatabaseReference userICchat = FirebaseDatabase.getInstance().getReference().child("Chat").child(matchId);
                        
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                        if (mRecyclerView.getAdapter() != null && resultsChat.size()>0)
                            mRecyclerView.smoothScrollToPosition(resultsChat.size() -1);
                        else
                            Toast.makeText(ChatActivity.this, "chat empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private ArrayList<ChatObject> resultsChat = new ArrayList<>();

    private List<ChatObject> getDataSetChat(){
        return resultsChat;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
    }
}