package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void back(View view) {
        finish();
    }



    public void acction(View view) {
        Intent intent = new Intent(About.this, Privacy_Terms_AboutUs.class);
        switch (view.getId()) {
            case R.id.privacy:
                intent.putExtra("witch", "privacy");
                break;

            case R.id.terms:
                intent.putExtra("witch", "terms");
                break;

            case R.id.about:
                intent.putExtra("witch", "about");
                break;
        }

        startActivity(intent);
    }
}
