package com.example.GreenHouse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.GreenHouse.ml.Eye;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DiseaseDetectionActivity extends AppCompatActivity {

    Button gallery, camera;
    ImageView imageView;

    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detection);


        gallery = findViewById(R.id.button);
        camera = findViewById(R.id.camerabtn);
        imageView = findViewById(R.id.imageView2);
        result = findViewById(R.id.textView);


        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(DiseaseDetectionActivity.this)
                        .compress(256)
                        .galleryOnly()
                        .maxResultSize(512, 512)
                        .start();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(DiseaseDetectionActivity.this)
                        .compress(256)
                        .cameraOnly()
                        .maxResultSize(512, 512)
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK || requestCode == 250) {
            result.setText("Loading");
            Uri uri = null;
            if (data != null) {
                uri = data.getData();


                //try {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
                Eye model = null;
                try {
                    model = Eye.newInstance(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Creates inputs for reference.
                TensorImage image = TensorImage.fromBitmap(bitmap);

                // Runs model inference and gets result.

                Eye.Outputs outputs = model.process(image);

                ArrayList<String> list = new ArrayList<>();
                list.add("Normal Eye");
                list.add("Cataract");
                list.add("Glaucoma");
                list.add("Retina");
                List<Category> probability = outputs.getProbabilityAsCategoryList();

                // Releases model resources if no longer used.


                // Releases model resources if no longer used.
                model.close();

                StringBuilder builder = new StringBuilder();
                for (Category cat :
                        probability) {

                    String label = "";
                    for (String tmp : list) {
                        if (cat.getLabel().toLowerCase(Locale.ROOT).contains(tmp.toLowerCase(Locale.ROOT)) || cat.getLabel().toLowerCase(Locale.ROOT).contains("1_normal")) {
                            label = tmp;
                            Log.d("clima", "ll1");
                            break;
                        }
                    }


                    double d = cat.getScore() * 100;
                    DecimalFormat f = new DecimalFormat("##.0");

                    builder.append(label).append("\t ").append(f.format(d)).append("%\n");
                    Log.d("clima name", cat.getDisplayName());
                    Log.d("clima label", cat.getLabel());
                    Log.d("clima score", String.valueOf(cat.getScore()));
                }
                result.setText(builder.toString());
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        }
    }


    public final int getMax(float[] arr) {
        int ind = 0;
        float min = 0.0F;
        int i = 0;

        for (int var5 = arr.length - 1; i <= var5; ++i) {
            if (arr[i] > min) {
                min = arr[i];
                ind = i;
            }
        }

        return ind;
    }

}