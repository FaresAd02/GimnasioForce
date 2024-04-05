package com.example.runnerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityEditarPerfil extends AppCompatActivity {

    private EditText txtNombrePerfil, txtPasswordPerfil, txtEdadPerfil;
    private Spinner spnPais;
    private Button btnEditar;
    private DatabaseReference databaseReference;
    private ImageView imageViewPerfil;
    private String databaseUid;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                processBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    private final ActivityResultLauncher<Intent> takePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Bundle extras = result.getData().getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            processBitmap(imageBitmap);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarperfil);
        initializeUI();
        databaseUid = getIntent().getStringExtra("DATABASE_UID");
        loadData();
    }

    private void initializeUI() {
        txtNombrePerfil = findViewById(R.id.txtNombrePerfil);
        txtPasswordPerfil = findViewById(R.id.txtPasswordPerfil);
        txtEdadPerfil = findViewById(R.id.txtEdadPerfil);
        spnPais = findViewById(R.id.spnPais1);
        btnEditar = findViewById(R.id.btnEditar);
        imageViewPerfil = findViewById(R.id.imageView9);

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuario");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        spnPais.setAdapter(adapter);

        findViewById(R.id.btnSubir).setOnClickListener(view -> showChangePhotoDialog());
        btnEditar.setOnClickListener(view -> updateUserInfo());
    }

    private void loadData() {
        if (databaseUid != null) {
            databaseReference.child(databaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("nombres").getValue(String.class);
                        String password = snapshot.child("password").getValue(String.class);
                        String age = snapshot.child("edad").getValue(String.class);
                        String country = snapshot.child("pais").getValue(String.class);
                        String imageBase64 = snapshot.child("imagenPerfilUri").getValue(String.class);

                        txtNombrePerfil.setText(name);
                        txtPasswordPerfil.setText(password);
                        txtEdadPerfil.setText(age);
                        if (country != null) {
                            int position = ((ArrayAdapter<String>)spnPais.getAdapter()).getPosition(country);
                            spnPais.setSelection(position);
                        }

                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imageViewPerfil.setImageBitmap(decodedByte);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ActivityEditarPerfil.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showChangePhotoDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                        } else {
                            takePhoto.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        }
                    } else {
                        pickImage.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    }
                })
                .show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        } else {
            Toast.makeText(this, "Camera permission is required to use camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processBitmap(Bitmap bitmap) {
        String imageBase64 = bitmapToBase64(bitmap);
        imageViewPerfil.setImageBitmap(bitmap);
        saveImageBase64ToFirebase(imageBase64);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveImageBase64ToFirebase(String imageBase64) {
        if (databaseUid != null) {
            databaseReference.child(databaseUid).child("imagenPerfilUri").setValue(imageBase64)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ActivityEditarPerfil.this, "Image saved successfully.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ActivityEditarPerfil.this, "Failed to save image.", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateUserInfo() {
        String name = txtNombrePerfil.getText().toString().trim();
        String password = txtPasswordPerfil.getText().toString().trim();
        String age = txtEdadPerfil.getText().toString().trim();
        String country = spnPais.getSelectedItem().toString();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("nombres", name);
        userUpdates.put("password", password);
        userUpdates.put("edad", age);
        userUpdates.put("pais", country);

        if (databaseUid != null) {
            databaseReference.child(databaseUid).updateChildren(userUpdates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ActivityEditarPerfil.this, "User info updated successfully.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ActivityEditarPerfil.this, "Failed to update user info.", Toast.LENGTH_SHORT).show());
        }
    }
}
