package com.skin.skinscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.tomer.fadingtextview.FadingTextView;

public class HomeActivity extends AppCompatActivity {

    TextView btn;

    FadingTextView fadingTextView;
    String[] text
            = { "Are you seeking skin disease?",
            "Don't know about your diseases?",
            "Check your skin for free" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        btn=findViewById(R.id.id_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(HomeActivity.this,MainActivity.class);
                startActivity(i);
            }
        });

        fadingTextView = findViewById(R.id.fadingTextView);
        fadingTextView.setTexts(text);

    }
}