package com.example.needlove.Adapters;


import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.MatcherFrag;
import com.example.needlove.Model.UsersDiscover;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;


public class SwipAdapter extends ArrayAdapter<UsersDiscover>{
    public SwipAdapter(Context context, ArrayList<UsersDiscover> users) {

        super(context, 0, users);

    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position

        UsersDiscover user = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_swip, parent, false);

        }

        // Lookup view for data population

        TextView username = convertView.findViewById(R.id.item_discover_username);
        TextView userage = convertView.findViewById(R.id.discover_user_age);
        TextView country = convertView.findViewById(R.id.item_discover_away);
        ImageView userprofileimg = convertView.findViewById(R.id.discover_user_img);

        // Populate the data into the template view using the data object

        username.setText(user.getUsername());
        userage.setText(String.valueOf(user.getAge()));
        country.setText(user.getCountry());
        Picasso.get().load(user.getProfileimage()).placeholder(R.drawable.avatar_prf).into(userprofileimg);

        // Return the completed view to render on screen


        return convertView;

    }

}
