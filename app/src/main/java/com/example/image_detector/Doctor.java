package com.example.image_detector;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;

public class Doctor extends AppCompatActivity {

    private TextInputEditText inputEditText;
    private MaterialButton submitButton;
    private TextView outputTextView;
    private GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        inputEditText = findViewById(R.id.inputEditText);
        submitButton = findViewById(R.id.submitButton);
        outputTextView = findViewById(R.id.outputTextView);

        GenerativeModel generativeModel = new GenerativeModel(
                "gemini-pro",
                "AIzaSyBiweBVuI-PHJM6R1FuiMXMkVWAYnyNCBU"
        );
        model = GenerativeModelFutures.from(generativeModel);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = inputEditText.getText().toString();
                if (!userInput.isEmpty()) {
                    outputTextView.setText("Thinking...");
                    submitButton.setEnabled(false);

                    executor.execute(() -> {
                        try {
                            Part part = new TextPart(userInput);
                            Content content = new Content(Collections.singletonList((TextPart) part));
                            GenerateContentResponse response = model.generateContent(content).get();
                            String generatedText = response.getText();

                            runOnUiThread(() -> {
                                outputTextView.setText(generatedText);
                                submitButton.setEnabled(true);
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                outputTextView.setText("Error: " + e.getMessage());
                                submitButton.setEnabled(true);
                            });
                        }
                    });
                }
            }
        });
    }
}