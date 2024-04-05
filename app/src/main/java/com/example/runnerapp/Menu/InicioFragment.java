package com.example.runnerapp.Menu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class InicioFragment extends Fragment {
    private View vista;
    private TextView txtKm, txtMinKm, txtDuracion;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ImageView imageViewPerfil;
    public InicioFragment() {
    }

    public static InicioFragment newInstance() {
        Bundle args = new Bundle();
        InicioFragment fragment = new InicioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_inicio, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        txtKm = vista.findViewById(R.id.txtKm);
        txtMinKm = vista.findViewById(R.id.txtMinKm);
        txtDuracion = vista.findViewById(R.id.txtDuracion);
        imageViewPerfil = vista.findViewById(R.id.imageView4);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            loadDataFromFirebase(user.getEmail());
        }

        return vista;
    }
    private void loadDataFromFirebase(String userEmail) {
        DatabaseReference datosRef = databaseReference.child("Datos");
        Query query = datosRef.orderByChild("email").equalTo(userEmail);
        DatabaseReference usersRef = databaseReference.child("Usuario");
        Query queryusers = usersRef.orderByChild("email").equalTo(userEmail);
        queryusers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String imageBase64 = userSnapshot.child("imagenPerfilUri").getValue(String.class);
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageViewPerfil.setImageBitmap(decodedByte);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("loadProfileImageByEmail", "Error loading user image: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error loading profile image.", Toast.LENGTH_SHORT).show();
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                float maxDistance = 0;
                String maxDuration = "0:00";
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String currentDuration = snapshot.child("minutes").getValue(String.class);
                    long currentDurationSeconds = convertDurationToSeconds(currentDuration);

                    Float currentDistance = snapshot.child("metros").getValue(Float.class);
                    if(currentDistance != null && currentDistance > maxDistance) {
                        maxDistance = currentDistance;
                    }

                    if (currentDurationSeconds > convertDurationToSeconds(maxDuration)) {
                        maxDuration = currentDuration;
                    }
                }

                if (maxDistance > 0) {
                    txtKm.setText(String.format(Locale.getDefault(), "%.2f", maxDistance / 1000));
                }

                if (!maxDuration.isEmpty()) {
                    long maxDurationInSeconds = convertDurationToSeconds(maxDuration);
                    txtDuracion.setText(formatDurationToHHMMSS(maxDurationInSeconds));

                    if(maxDistance > 0) {
                        float minutesPerKm = (maxDurationInSeconds / 60) / (maxDistance / 1000);
                        txtMinKm.setText(String.format(Locale.getDefault(), "%.2f ", minutesPerKm));
                    }
                } else {
                    Log.d("InicioFragment", "No se encontraron datos.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("InicioFragment", "Error al cargar datos: " + databaseError.getMessage());
            }
        });
    }


    private boolean isGreaterDuration(String currentDuration, String maxDuration) {
        long currentDurationSeconds = convertDurationToSeconds(currentDuration);
        long maxDurationSeconds = convertDurationToSeconds(maxDuration);

        return currentDurationSeconds > maxDurationSeconds;
    }



    private long convertDurationToSeconds(String durationString) {
        long totalSeconds = 0;
        if (durationString != null && !durationString.isEmpty()) {
            String[] parts = durationString.trim().replace(" y ", ", ").split(", ");
            for (String part : parts) {
                String[] timeParts = part.split(" ");
                if (timeParts.length == 2) {
                    try {
                        int number = Integer.parseInt(timeParts[0]);
                        if (part.contains("minuto") || part.contains("minutos")) {
                            totalSeconds += Integer.parseInt(timeParts[0]) * 60;
                        } else if (part.contains("segundo") || part.contains("segundos")) {
                            totalSeconds += Integer.parseInt(timeParts[0]);
                        }
                    } catch (NumberFormatException e) {
                        Log.e("InicioFragment", "convertDurationToSeconds: Invalid number format for part: " + part, e);
                    }
                }
            }
        }
        return totalSeconds;
    }

    private String formatDurationToHHMMSS(long totalSeconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % TimeUnit.HOURS.toMinutes(1);
        long seconds = totalSeconds % TimeUnit.MINUTES.toSeconds(1);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

}

