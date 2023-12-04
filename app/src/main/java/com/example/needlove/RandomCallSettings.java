package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class RandomCallSettings extends AppCompatActivity {
    RadioGroup radioGroup_lookingfor;
    RadioButton radio_male, radio_female, radio_both;

    TextView countryTxt;
    CheckBox checkAllcountry;
    CountryCodePicker ccp;
    String countrySelectName = null;
    String countrySelectNamecode = null;

    String LookingForr = "Both";

    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_call_settings);


        radioGroup_lookingfor = findViewById(R.id.radioGroup_lookingfor);
        radio_male = findViewById(R.id.gender_male);
        radio_female = findViewById(R.id.gender_female);
        radio_both = findViewById(R.id.gender_both);
        countryTxt = findViewById(R.id.countryTxtView);
        checkAllcountry = findViewById(R.id.checkAllcountry);


        ccp = findViewById(R.id.ccp);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.raleway_medium);
        ccp.setTypeFace(typeface);

        pref = getSharedPreferences("call_filter", Context.MODE_PRIVATE);


        if (pref.contains("country_code")) {
            String country_code = pref.getString("country_code", "");
            String country_name = pref.getString("country", "");
            String lookingFor = pref.getString("looking_for", "");

            LookingForr = lookingFor;

            if (!country_name.equals("all")) {
                checkAllcountry.setChecked(false);
                countrySelectName = country_name;
                countrySelectNamecode = country_code;
                ccp.setCountryForNameCode(country_code);
            } else {
                ccp.setVisibility(View.GONE);
                countryTxt.setVisibility(View.VISIBLE);
                countrySelectName = country_name;
                countrySelectNamecode = country_code;
                countryTxt.setText(country_name);
            }



            getlookingfor(lookingFor);


        } else {

            radio_both.setChecked(true);
            LookingForr = "Both";
            ccp.setVisibility(View.GONE);
            countryTxt.setVisibility(View.VISIBLE);
            countryTxt.setText("All country");
        }


        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countrySelectName = ccp.getSelectedCountryName();
                countrySelectNamecode = ccp.getSelectedCountryNameCode();
                checkAllcountry.setChecked(false);

                ccp.setVisibility(View.VISIBLE);
                countryTxt.setVisibility(View.GONE);
            }
        });

    }

    public void saveFilter(View view) {

        SharedPreferences.Editor editor = pref.edit();

        if (checkAllcountry.isChecked()  && radio_both.isChecked()) {
            editor.clear();
            editor.apply();
            finish();

        } else {

            if (checkAllcountry.isChecked()) {
                editor.putString("country", "all");
                editor.putString("country_code", "0");
            } else {
                editor.putString("country", countrySelectName);
                editor.putString("country_code", countrySelectNamecode);
            }






            editor.putString("looking_for", getRadioChecked());
            editor.putInt("filter_cat", getFilter());

            editor.apply();
            finish();


        }
    }

    public void contrytxxt(View view) {
        view.setVisibility(View.GONE);
        ccp.setVisibility(View.VISIBLE);
        ccp.launchCountrySelectionDialog();
    }

    public void backkk(View view) {
        finish();
    }

    public void chekkcountry(View view) {
        if (checkAllcountry.isChecked()) {
            countryTxt.setText("All country");
            countryTxt.setVisibility(View.VISIBLE);
            ccp.setVisibility(View.GONE);
        } else {
            ccp.launchCountrySelectionDialog();
            checkAllcountry.setChecked(true);
        }
    }



    private void getlookingfor(String lookingfor) {
        if (lookingfor.equals("Male")) {
            radio_male.setChecked(true);
        } else if (lookingfor.equals("Female")) {
            radio_female.setChecked(true);
        } else if (lookingfor.equals("Both")) {
            radio_both.setChecked(true);
        }

    }


    String getRadioChecked() {
        if (radio_female.isChecked()) {
            return "Female";
        } else if (radio_male.isChecked()) {
            return "Male";
        } else {
            return "Both";
        }
    }


    int getFilter() {
        int reqct = 0;

        if (checkAllcountry.isChecked() && !radio_both.isChecked())//
        {
            reqct = 1;
        } else if (!checkAllcountry.isChecked() && radio_both.isChecked())//
        {
            reqct = 2;
        } else if (!checkAllcountry.isChecked() && !radio_both.isChecked())//
        {
            reqct = 3;
        }

        return reqct;
    }
}
