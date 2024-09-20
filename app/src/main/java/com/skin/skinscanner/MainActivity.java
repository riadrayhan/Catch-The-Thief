package com.skin.skinscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tomer.fadingtextview.FadingTextView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    ImageView imgage1;
    TextView textView1,textView2;

    FadingTextView fadingTextView;
    String[] text = { "Are you seeking skin disease?",
            "Don't know about your diseases?",
            "Check your skin for free" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fadingTextView = findViewById(R.id.fadingTextView);
        fadingTextView.setTexts(text);

        imgage1=findViewById(R.id.img1);
        textView1=findViewById(R.id.card_title2);
        textView2=findViewById(R.id.card_title3);

        imgage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this, About_Mpox.class);
                startActivity(i);
            }
        });
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Clicked About Skin Scanner page",Toast.LENGTH_SHORT).show();
                Intent i=new Intent(MainActivity.this, About_App.class);
                startActivity(i);
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Privacy policy",Toast.LENGTH_SHORT).show();
                Intent i=new Intent(MainActivity.this, Privacy_policy.class);
                startActivity(i);
            }
        });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "TechLab Doctor is ready to measure your diseases", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_skin) {
                    Toast.makeText(MainActivity.this, "Near Hospital", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, SkinActivity.class);
                    startActivity(i);
                    return true;
                }else if (itemId == R.id.map) {
                    Toast.makeText(MainActivity.this, "Near Hospital", Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/hospital+near+me/@23.7736179,90.3650526,16z/data=!3m1!4b1?entry=ttu"));
                    startActivity(i);
                    return true;
                }else if (itemId == R.id.nav_doctor) {
                    Toast.makeText(MainActivity.this, "Ask anything with TechLab Doctor", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, com.skin.skinscanner.Doctor.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });

        animateBottomNavigationView();
    }

    private void animateBottomNavigationView() {
        bottomNavigationView.setAlpha(0f);
        bottomNavigationView.setTranslationY(100);
        bottomNavigationView.animate()
                .setDuration(1000)
                .translationY(0)
                .alpha(1f)
                .setListener(null);
    }


}
