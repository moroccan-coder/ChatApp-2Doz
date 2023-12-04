package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;


public class discover_settings extends AppCompatActivity {
    CountryCodePicker ccp;
    String countrySelectName = null;
    String countrySelectNamecode = null;

    String LookingForr = "Both";

    int ageMin = 18;
    int ageMax = 18;
    LinearLayout lnrseekbr1;
    LinearLayout lnrseekbr2;

    TextView countryTxt;
    CheckBox checkAllage;
    CheckBox checkAllcountry;
    CheckBox checkAllOnline;


    TextView txtMin, txtMax;
    CrystalRangeSeekbar crystalRangeSeekbar;
    CrystalRangeSeekbar crystalRangeSeekbar2;


    RadioGroup radioGroup_lookingfor;
    RadioButton radio_male, radio_female, radio_both;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_settings);

        radioGroup_lookingfor = findViewById(R.id.radioGroup_lookingfor);
        radio_male = findViewById(R.id.gender_male);
        radio_female = findViewById(R.id.gender_female);
        radio_both = findViewById(R.id.gender_both);
        lnrseekbr1 = findViewById(R.id.lnrSekkbar1);
        lnrseekbr2 = findViewById(R.id.lnrSeekbar2);
        checkAllage = findViewById(R.id.checkAllage);
        checkAllcountry = findViewById(R.id.checkAllcountry);
        checkAllOnline = findViewById(R.id.checkAllOnline);
        countryTxt = findViewById(R.id.countryTxtView);


        // get seekbar from view
        crystalRangeSeekbar = findViewById(R.id.rangeSeekbar);
        crystalRangeSeekbar2 = findViewById(R.id.rangeSeekbar2);

        // get min and max text view
        txtMin = findViewById(R.id.textMin);
        txtMax = findViewById(R.id.textMax);

        ccp = findViewById(R.id.ccp);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.raleway_medium);
        ccp.setTypeFace(typeface);


        pref = getSharedPreferences("discover_filter", Context.MODE_PRIVATE);

        if (pref.contains("country_code")) {
            lnrseekbr1.setVisibility(View.VISIBLE);
            String country_code = pref.getString("country_code", "");
            String country_name = pref.getString("country", "");
            String lookingFor = pref.getString("looking_for", "");
            String connect = pref.getString("connect", "");
            LookingForr = lookingFor;
            ageMin = pref.getInt("age_min", 18);
            ageMax = pref.getInt("age_max", 100);

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

            if (pref.contains("age")) {
                lnrseekbr1.setVisibility(View.GONE);
            } else {
                lnrseekbr2.setVisibility(View.GONE);
                checkAllage.setChecked(false);
            }

            if (pref.contains("connect")) {
                if (connect.equals("online")) {
                    checkAllOnline.setChecked(true);
                } else {
                    checkAllOnline.setChecked(false);
                }
            }


            getlookingfor(lookingFor);
            txtMin.setText(String.valueOf(ageMin));
            txtMax.setText(String.valueOf(ageMax));

            crystalRangeSeekbar.setMinStartValue(ageMin);
            crystalRangeSeekbar.setMaxStartValue(ageMax);
            crystalRangeSeekbar.setFixGap(10);
            crystalRangeSeekbar.apply();

        } else {

            radio_both.setChecked(true);
            checkAllOnline.setChecked(false);
            LookingForr = "Both";
            ccp.setVisibility(View.GONE);
            countryTxt.setVisibility(View.VISIBLE);
            countryTxt.setText("All country");
            crystalRangeSeekbar.setMinStartValue(18);
            crystalRangeSeekbar.setMaxStartValue(28);
            crystalRangeSeekbar.setFixGap(10);
            crystalRangeSeekbar.apply();

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


// set listener
        crystalRangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                txtMin.setText(String.valueOf(minValue));
                txtMax.setText(String.valueOf(maxValue));

                ageMin = Integer.valueOf(String.valueOf(minValue));
                ageMax = Integer.valueOf(String.valueOf(maxValue));
            }
        });


// set final value listener
        crystalRangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
            }
        });


        crystalRangeSeekbar2.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                lnrseekbr2.setVisibility(View.GONE);
                lnrseekbr1.setVisibility(View.VISIBLE);
                checkAllage.setChecked(false);
                crystalRangeSeekbar2.setFixGap(100F);
            }
        });


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


    public void saveFilter(View view) {
        SharedPreferences.Editor editor = pref.edit();

        if (checkAllcountry.isChecked() && checkAllage.isChecked() && radio_both.isChecked() && !checkAllOnline.isChecked()) {
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

            if (checkAllage.isChecked()) {
                editor.putString("age", "all");
            } else {
                if (pref.contains("age")) {
                    editor.remove("age");
                    editor.apply();
                }
            }


            if (checkAllOnline.isChecked()) {
                editor.putString("connect", "online");
            } else {
                editor.putString("connect", "offline");
            }

            editor.putString("looking_for", getRadioChecked());
            editor.putInt("age_min", ageMin);
            editor.putInt("age_max", ageMax);
            editor.putInt("filter_cat", getFilter());
            editor.putInt("age_range", getAgeRange());

            editor.apply();
            Toast.makeText(this, "Filters Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();


        }

    }

    private int getAgeRange() {

        int ageRange = 0;

        if (!checkAllage.isChecked()) {
            if (ageMin == 18) {
                ageRange = 1;
            } else if (ageMin == 28) {
                ageRange = 2;
            } else if (ageMin == 38) {
                ageRange = 3;
            } else if (ageMin == 48) {
                ageRange = 4;
            } else if (ageMin == 58) {
                ageRange = 5;
            } else if (ageMin == 68) {
                ageRange = 6;
            } else if (ageMin == 78) {
                ageRange = 7;
            } else if (ageMin == 88) {
                ageRange = 8;
            }
        } else {
            ageRange = 0;
        }


        return ageRange;
    }


    int getFilter() {
        int reqct = 0;

        if (checkAllcountry.isChecked() && !radio_both.isChecked() && !checkAllage.isChecked())//
        {
            reqct = 1;
        } else if (checkAllcountry.isChecked() && !radio_both.isChecked() && checkAllage.isChecked())//
        {
            reqct = 2;
        } else if (!checkAllcountry.isChecked() && !radio_both.isChecked() && !checkAllage.isChecked())//
        {
            reqct = 3;
        } else if (!checkAllcountry.isChecked() && radio_both.isChecked() && !checkAllage.isChecked())//
        {
            reqct = 4;
        } else if (!checkAllcountry.isChecked() && radio_both.isChecked() && checkAllage.isChecked())//
        {
            reqct = 5;
        } else if (checkAllcountry.isChecked() && radio_both.isChecked() && !checkAllage.isChecked())//
        {
            reqct = 6;
        } else if (!checkAllcountry.isChecked() && !radio_both.isChecked() && checkAllage.isChecked())//
        {
            reqct = 7;
        }

        else if (checkAllcountry.isChecked() && !radio_both.isChecked() && !checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 8;
        } else if (checkAllcountry.isChecked() && !radio_both.isChecked() && checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 9;
        } else if (!checkAllcountry.isChecked() && !radio_both.isChecked() && !checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 10;
        } else if (!checkAllcountry.isChecked() && radio_both.isChecked() && !checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 11;
        } else if (!checkAllcountry.isChecked() && radio_both.isChecked() && checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 12;
        } else if (checkAllcountry.isChecked() && radio_both.isChecked() && !checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 13;
        } else if (!checkAllcountry.isChecked() && !radio_both.isChecked() && checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 14;
        }
        else if (checkAllcountry.isChecked() && radio_both.isChecked() && checkAllage.isChecked() && checkAllOnline.isChecked())//
        {
            reqct = 15;
        }

        return reqct;
    }


    public void backkk(View view) {
        finish();

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


    public void contrytxxt(View view) {
        view.setVisibility(View.GONE);
        ccp.setVisibility(View.VISIBLE);
        ccp.launchCountrySelectionDialog();
    }


    public void chekk(View view) {

        if (checkAllage.isChecked()) {
            lnrseekbr2.setVisibility(View.VISIBLE);
            lnrseekbr1.setVisibility(View.GONE);
        } else {
            lnrseekbr2.setVisibility(View.GONE);
            lnrseekbr1.setVisibility(View.VISIBLE);
        }
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
}
