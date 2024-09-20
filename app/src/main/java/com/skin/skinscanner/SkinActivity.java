package com.skin.skinscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skin.skinscanner.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SkinActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    private TextView result;
    private ImageView imageView;
    private Button pictureButton;
    private Button galleryButton,realtime;
    private BottomNavigationView bottomNavigationView;
    private final int imageSize = 224;

    private String[] labels = {"Mpox", "Normal Skin", "Invalid Data"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_skin);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        pictureButton = findViewById(R.id.button);
        galleryButton = findViewById(R.id.id_gallery_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SkinActivity.this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(SkinActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_skin) {
                    Toast.makeText(SkinActivity.this, "Scan your Skin", Toast.LENGTH_SHORT).show();
                    return true;
                }
               else if (itemId == R.id.nav_home) {
                    Intent i = new Intent(SkinActivity.this, com.skin.skinscanner.MainActivity.class);
                    startActivity(i);
                    Toast.makeText(SkinActivity.this, "TechLab Doctor is ready to measure your diseases", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (itemId == R.id.map) {
                    Toast.makeText(SkinActivity.this, "Near Hospital", Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/hospital+near+me/@23.7736179,90.3650526,16z/data=!3m1!4b1?entry=ttu"));
                    startActivity(i);
                    return true;
                }else if (itemId == R.id.nav_doctor) {
                    Toast.makeText(SkinActivity.this, "Ask anything with TechLab Doctor", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(SkinActivity.this, Doctor.class);
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

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CAMERA_REQUEST_CODE || requestCode == GALLERY_REQUEST_CODE) && resultCode == RESULT_OK && data != null) {
            Bitmap image = null;
            if (requestCode == CAMERA_REQUEST_CODE) {
                image = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }

            if (image != null) {
                imageView.setImageBitmap(image);
                try {
                    Bitmap resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
                    classifyImage(resizedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            float confidenceThreshold = 0.5f;
            if (maxConfidence >= confidenceThreshold) {
                result.setText(labels[maxPos] + " (" + String.format("%.2f", maxConfidence * 100) + "%)");
            } else {
                result.setText("Invalid object detected");
            }

            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error classifying image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
