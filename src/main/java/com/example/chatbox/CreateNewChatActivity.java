package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.chatbox.user.UserAdapter;
import com.example.chatbox.user.UserObject;
import com.example.chatbox.utils.CountryToPhonePrefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateNewChatActivity extends AppCompatActivity {

    Button mCancelButton,mCreateChat;
    EditText mChatName;

    RecyclerView mUserList;
    RecyclerView.Adapter<UserAdapter.ViewHolder> mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> contactsList;
    ArrayList<UserObject> userList;

    HashMap<String,Boolean> userDisplayed;
    //this is to make sure that if the user has 2 contacts with same number,only one of them is displayed.




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);


        userList=new ArrayList<>();
        contactsList=new ArrayList<>();
        userDisplayed= new HashMap<>();


        initializeViews();
        initializeRecyclerViews();

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AllChatsActivity.class));
            }
        });



        mCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChatName!=null) {
                    String s = mChatName.getText().toString().trim();
                    if (s.length() != 0) {
                        createChat();
                        startActivity(new Intent(getApplicationContext(),AllChatsActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Give the name to the chat mf", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        getContactList();
    }

    private void createChat() {

        DatabaseReference mChatDB= FirebaseDatabase.getInstance().getReference().child("Chats");
        String key= mChatDB.push().getKey();

        DatabaseReference mUserDB=FirebaseDatabase.getInstance().getReference().child("Users");

        mChatDB=mChatDB.child(key).child("info");

        HashMap<String,Object> mChatInfo=new HashMap<>();



        int count = 0;
        for(UserObject user:userList){
            if(user.isSelected()){
                if(user.getUid()!=null) {
                    count = count+1;
                    mChatInfo.put("user/"+user.getUid(), "true");
                    mUserDB.child(user.getUid()).child("chat").child(key).setValue("true");
                }
            }
        }
        FirebaseUser mUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mUser!=null) {
            count = count+1;
            mChatInfo.put("user/"+mUser.getUid(),"true");
            mUserDB.child(mUser.getUid()).child("chat").child(key).setValue("true");
        }

        mChatInfo.put("Name",mChatName.getText().toString());
        mChatInfo.put("ID",key);
        mChatInfo.put("Number Of Users",count);
        mChatDB.updateChildren(mChatInfo);

    }

    private void getContactList() {

        Cursor cursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        //Cursor cursor1=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);

        while(cursor!=null && cursor.moveToNext()){
            String phoneNumber=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));


            phoneNumber=phoneNumber.replace(" ","");
            phoneNumber=phoneNumber.replace("-","");
            phoneNumber=phoneNumber.replace("(","");
            phoneNumber=phoneNumber.replace(")","");
            phoneNumber=phoneNumber.replace("/","");
            phoneNumber=phoneNumber.replace("#","");

            if(phoneNumber.charAt(0)!='+'){
                phoneNumber = getIsoPrefix()+phoneNumber;
            }



                if (name != null && userDisplayed.get(phoneNumber) == null) {
                    userDisplayed.put(phoneNumber, true);
                    UserObject contactUser = new UserObject(name, phoneNumber);
                    checkUserDetails(contactUser);

                }

        }
    }

    private String getIsoPrefix() {

        String iso="";

        TelephonyManager telephonyManager=(TelephonyManager)getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if(telephonyManager!=null){
            iso=CountryToPhonePrefix.prefixFor(telephonyManager.getNetworkCountryIso());
        }

        return iso;

    }

    private void checkUserDetails(final UserObject contactUser) {
        DatabaseReference mUserDB= FirebaseDatabase.getInstance().getReference().child("Users");
        Query query=mUserDB.orderByChild("Phone Number").equalTo(contactUser.getPhoneNumber());
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        final String[] currentPhoneNumber = {""};
        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPhoneNumber[0] = snapshot.child(mUser.getUid()).child("Phone Number").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    for(DataSnapshot childSnapshot:snapshot.getChildren()){
                        if(childSnapshot.getKey()!=null && childSnapshot.child("Name").getValue()!=null  && childSnapshot.child("Phone Number").getValue()!=null ){
                            if(!currentPhoneNumber[0].equals(childSnapshot.child("Phone Number").getValue())) {
                                if (childSnapshot.child("Profile Image Uri").getValue() != null) {
                                    UserObject user = new UserObject(childSnapshot.getKey(), contactUser.getName(), contactUser.getPhoneNumber(), childSnapshot.child("Profile Image Uri").getValue().toString());
                                    userList.add(user);
                                } else {
                                    UserObject user = new UserObject(childSnapshot.getKey(), contactUser.getName(), contactUser.getPhoneNumber());
                                    userList.add(user);
                                }
                                mUserListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"something went wrong, please try again later",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initializeRecyclerViews() {
        mUserList=findViewById(R.id.recyclerViewList);
        mUserList.setHasFixedSize(false);
        mUserList.setNestedScrollingEnabled(false);



        mUserListAdapter= new UserAdapter(userList,this);
        mUserList.setAdapter(mUserListAdapter);

        mUserListLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        mUserList.setLayoutManager(mUserListLayoutManager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),AllChatsActivity.class));
    }

    private void initializeViews() {
        mCancelButton=findViewById(R.id.cancelButton);
        mCreateChat = findViewById(R.id.newChat);

        mChatName=findViewById(R.id.chatName);

    }
}