
package com.example.chatbox.message;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.chatbox.ImageViewActivity;
import com.example.chatbox.R;

import java.util.ArrayList;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    ArrayList<MessageObject> messageList;

    String userKey;
    int numberOfUsers;

    Context context;
    float density;
    RecyclerView.LayoutManager mMessageListLayoutManager;

    public MessageAdapter(ArrayList<MessageObject> messageList, Context context){
        this.messageList=messageList;
        this.context=context;
        userKey= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    public MessageAdapter(ArrayList<MessageObject> messageList, Context context, int numberOfUsers){
        this.messageList=messageList;
        this.context=context;
        this.numberOfUsers=numberOfUsers;
        userKey= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    public MessageAdapter(ArrayList<MessageObject> messageList, Context context, int numberOfUsers, float density, RecyclerView.LayoutManager mMessageListLayoutManager){
        this.messageList=messageList;
        this.density=density;
        this.context=context;
        this.numberOfUsers=numberOfUsers;
        this.mMessageListLayoutManager=mMessageListLayoutManager;
        userKey= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,null,false);

        RecyclerView.LayoutParams layoutParams= new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, final int position) {

        int count = messageList.size();


        holder.messageText.setText(messageList.get(position).getText());
        holder.messageSender.setText(messageList.get(position).getSenderName());
        holder.messageTime.setText(messageList.get(position).getTime());
        if(messageList.get(position).getText().equals("")){
            RelativeLayout.LayoutParams layoutParams1=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams1.addRule(RelativeLayout.ALIGN_END,R.id.mediaImage);
            layoutParams1.addRule(RelativeLayout.BELOW,R.id.mediaImage);
            holder.messageTime.setLayoutParams(layoutParams1);
            holder.messageText.setVisibility(View.GONE);
        }
        if((position!=0 && messageList.get(position).getSenderName().equals(messageList.get(position-1).getSenderName())) || numberOfUsers<=2 || messageList.get(position).getSenderId().equals(userKey)){
            holder.messageSender.setVisibility(View.GONE);
        }
        if(position==0 || !messageList.get(position).getDate().equals(messageList.get(position - 1).getDate())){
            holder.messageDate.setText(messageList.get(position).getDate());
            holder.messageDate.setVisibility(View.VISIBLE);
        }
        holder.messageText.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        float width = holder.messageText.getMeasuredWidth();
        if(width>240){
            RelativeLayout.LayoutParams layoutParams1=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams1.addRule(RelativeLayout.ALIGN_END,R.id.messageText);
            layoutParams1.addRule(RelativeLayout.BELOW,R.id.messageText);
            holder.messageTime.setLayoutParams(layoutParams1);
        }
        if(!messageList.get(position).getImageUri().equals("")) {
            holder.mediaImage.setVisibility(View.VISIBLE);
            holder.mediaImage.setClipToOutline(true);
            if(!messageList.get(position).getText().equals("")){
                RelativeLayout.LayoutParams layoutParams1=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.addRule(RelativeLayout.ALIGN_END,R.id.mediaImage);
                layoutParams1.addRule(RelativeLayout.BELOW,R.id.messageText);
                holder.messageTime.setLayoutParams(layoutParams1);
                holder.messageText.setVisibility(View.GONE);
            }

            holder.mediaImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(context, ImageViewActivity.class);
                    intent.putExtra("URI",messageList.get(position).getImageUri());
                    context.startActivity(intent);
                }
            });
            Glide.with(context).load(Uri.parse(messageList.get(position).getImageUri())).into(holder.mediaImage);
        }
        if(messageList.get(position).getSenderId().equals(userKey)){
            RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) holder.relativeLayout.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.relativeLayout.setLayoutParams(layoutParams);
            holder.relativeLayout.setBackgroundResource(R.drawable.custom_background_message_reciever);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                messageList.remove(position);
                                notifyDataSetChanged();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView    messageText,
                messageSender,
                messageTime,
                messageDate;

        ImageView mediaImage;

        RelativeLayout relativeLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSender=itemView.findViewById(R.id.messageSender);
            messageText=itemView.findViewById(R.id.messageText);
            messageTime=itemView.findViewById(R.id.messageTime);
            mediaImage=itemView.findViewById(R.id.mediaImage);
            messageDate=itemView.findViewById(R.id.DateView);
            relativeLayout=itemView.findViewById(R.id.parentRelativeLayout);
        }
    }
}
