package com.example.image_detector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.image_detector.ml.Detect;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.image_detector.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 1;

    private TextView result;
    private ImageView imageView;
    private Button pictureButton;
    private BottomNavigationView bottomNavigationView;
    private int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        pictureButton = findViewById(R.id.button);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "TechLab Doctor is ready to measure your diseases", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_detect) {
                    Toast.makeText(MainActivity.this, "Keep your finger on the camera in 30 seconds", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, HeartRateMonitor.class);
                    startActivity(i);
                    return true;
                } else if (itemId == R.id.map) {
                    Toast.makeText(MainActivity.this, "Near Hospital", Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/hospital+near+me/@23.7736179,90.3650526,16z/data=!3m1!4b1?entry=ttu"));
                    startActivity(i);
                    return true;
                }else if (itemId == R.id.nav_doctor) {
                    Toast.makeText(MainActivity.this, "Ask anything with TechLab Doctor", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, Doctor.class);
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
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            if (image != null) {
                imageView.setImageBitmap(image);
                try {
                    Bitmap processedImage = preprocessImage(image);
                    Bitmap resizedImage = Bitmap.createScaledBitmap(processedImage, imageSize, imageSize, true);
                    classifyImage(resizedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap preprocessImage(Bitmap image) {
        // Convert to grayscale
        Bitmap grayImage = toGrayscale(image);

        // Enhance contrast (simple method, you might want to use a more sophisticated approach)
        Bitmap enhancedImage = enhanceContrast(grayImage);

        return enhancedImage;
    }

    private Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private Bitmap enhanceContrast(Bitmap bmp) {
        float[] colorTransform = {
                1.5f, 0, 0, 0, -20,
                0, 1.5f, 0, 0, -20,
                0, 0, 1.5f, 0, -20,
                0, 0, 0, 1, 0
        };

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(colorTransform);

        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);

        Bitmap enhancedBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(enhancedBitmap);
        canvas.drawBitmap(bmp, 0, 0, paint);

        return enhancedBitmap;
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

            String[] classes = {"Oily Skin", "Normal Skin", "Dry Skin", "Verruca", "Skin Allergy", "Acne", "Chicken Pox"};

            float confidenceThreshold = 0.7f;
            if (maxConfidence > confidenceThreshold) {
                result.setText(classes[maxPos] + " (" + String.format("%.2f", maxConfidence * 100) + "%)");
            } else {
                result.setText("Unable to classify with high confidence");
            }

            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error classifying image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}