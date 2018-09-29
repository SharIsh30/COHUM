package com.example.ishaan.cohum;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class zInformation extends AppCompatActivity {
    private StringBuffer buff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_z_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView txt = findViewById(R.id.infoText);
        Intent intent = getIntent();
        String p = "";
        String str = intent.getStringExtra("string");
        if (str.equals("pPolicy")) {
            p = getString(R.string.pPolicy);
        } else if (str.equals("TandC")) {
            p = getString(R.string.TandC);
        } else if (str.equals("geofence")) {
            Toast.makeText(this, "This article can be viewed again from the 'Help & Feedback' option.", Toast.LENGTH_LONG).show();
            p = getString(R.string.help_geofence);
        } else if (str.equals("tracking")) {
            Toast.makeText(this, "This article can be viewed again from the 'Help & Feedback' option.", Toast.LENGTH_LONG).show();
            p = getString(R.string.help_tracking);
        } else {
            p = "Try Again.";
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // for 24 api and more
            txt.setText(Html.fromHtml(p, Html.FROM_HTML_OPTION_USE_CSS_COLORS, new ImageGetter(), null));
        } else {
            // or for older api
            txt.setText(Html.fromHtml(p, new ImageGetter(), null));
        }

        txt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private class ImageGetter implements Html.ImageGetter {

        public Drawable getDrawable(String source) {
            int id;
            if (source.equals("geo_1.jpg")) {
                id = R.drawable.geo_1;
            } else if (source.equals("geo_2.jpg")) {
                id = R.drawable.geo_2;
            } else if (source.equals("geo_3.jpg")) {
                id = R.drawable.geo_3;
            } else if (source.equals("geo_4.jpg")) {
                id = R.drawable.geo_4;
            } else if (source.equals("geo_5.jpg")) {
                id = R.drawable.geo_5;
            } else if (source.equals("geo_6.jpg")) {
                id = R.drawable.geo_6;
            } else if (source.equals("geo_7.jpg")) {
                id = R.drawable.geo_7;
            } else if (source.equals("geo_8.jpg")) {
                id = R.drawable.geo_8;
            } else if (source.equals("geo_9.jpg")) {
                id = R.drawable.geo_9;
            } else if (source.equals("track_1.jpg")) {
                id = R.drawable.track_1;
            } else if (source.equals("track_2.jpg")) {
                id = R.drawable.track_2;
            } else if (source.equals("track_3.jpg")) {
                id = R.drawable.track_3;
            } else if (source.equals("track_4.jpg")) {
                id = R.drawable.track_4;
            } else {
                return null;
            }

            Drawable d = getResources().getDrawable(id);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}