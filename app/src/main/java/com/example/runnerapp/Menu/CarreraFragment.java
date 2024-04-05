package com.example.runnerapp.Menu;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.example.runnerapp.Clases.Datos;
import com.example.runnerapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CarreraFragment extends Fragment {private TextView stepsTextView;
    private DatabaseReference carreraRef;
    private TextView distanceTextView;
    private TextView dateTextView;
    private TextView caloriesTextView;

    private float previousX = 0f;
    private  double roundedCalories=0;
    private double roundedistance=0;

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private int stepsCount = 0;
    private double caloriesBurned=0;
    private boolean running = false;
    final float ACCELEROMETER_THRESHOLD = 30f;
    private float distanceCovered = 0;

    private static final float CALORIES_PER_STEP = 0.05f;







    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private int REQUEST_CODE = 111;
    View Vista;
    Button btn_start, btn_stop, btn_reset, btnfinalizar;
    Chronometer chronometro;
    Boolean correr = false;
    TextView mensaje;
    long detenerse;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    AwesomeValidation awesomeValidation;

    Button btnguardar;
    Double longitudOrigen, latitudOrigen, longitudFinal, latitudFinal;
    boolean dibujar = false;
    boolean finalizar = false;
    GoogleMap map;
    Boolean actualPosition = true;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CarreraFragment() {
    }

    public static CarreraFragment newInstance() {

        Bundle args = new Bundle();
        CarreraFragment fragment = new CarreraFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inicializarFirebase();
        Vista=inflater.inflate(R.layout.fragment_carrera, container, false);
        FirebaseAuth mauth  = FirebaseAuth.getInstance();
        FirebaseUser user = mauth.getCurrentUser();
        btn_start = Vista.findViewById(R.id.btn_start);
        btn_stop = Vista.findViewById(R.id.btn_stop);
        btn_reset = Vista.findViewById(R.id.btn_reset);
        chronometro = Vista.findViewById(R.id.chronometro);
        btnguardar = Vista.findViewById(R.id.BtnRegistrar);
        btnfinalizar = Vista.findViewById(R.id.btnFinalizar);
        mensaje = Vista.findViewById(R.id.txtmensaje);

        stepsTextView = Vista.findViewById(R.id.stepsTextView);
        distanceTextView = Vista.findViewById(R.id.distanceTextView);
        caloriesTextView = Vista.findViewById(R.id.caloriesTextView);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Button stopButton = Vista.findViewById(R.id.btnFinalizar);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = false;

            }
        });

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mauth  = FirebaseAuth.getInstance();
                FirebaseUser user = mauth.getCurrentUser();
                String mail = user.getEmail();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());
                String min = chronometro.getContentDescription().toString();

                Datos d = new Datos();
                d.setUid(UUID.randomUUID().toString());
                d.setEmail(mail);
                d.setDatenow(strDate);
                d.setMinutes(chronometro.getContentDescription().toString());
                d.setPasos(stepsCount);
                d.setCalorias(roundedCalories);
                d.setMetros(roundedistance);


                databaseReference.child("Datos").child(d.getUid()).setValue(d);
                Toast.makeText(getContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                resetChronometro();
            }
        });




        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(CarreraFragment.this.getContext());

        if (ActivityCompat.checkSelfPermission(CarreraFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            startChronometro();
            running = true;



            getFirstLocation();
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions((Activity) CarreraFragment.this.getContext(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChronometro();
                btn_start.setVisibility(View.INVISIBLE);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                stopChronometro();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.INVISIBLE);
                resetChronometro();
            }
        });

        btnfinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLastLocation();
                stopChronometro();
                mensaje.setVisibility(View.VISIBLE);
                btn_start.setVisibility(View.INVISIBLE);
                btn_stop.setVisibility(View.INVISIBLE);
                btnguardar.setVisibility(View.VISIBLE);
                btnfinalizar.setVisibility(View.INVISIBLE);
            }
        });
        return Vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (running) {
                float x = event.values[0];
                float deltaX = Math.abs(x - previousX);
                if (deltaX > ACCELEROMETER_THRESHOLD) {
                    stepsCount++;
                    updateSteps();
                    updateDistance();
                    updateCalories();

                }
                previousX = x;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Fecha: " + currentDate);
    }

    private void updateSteps() {
        stepsTextView.setText("Pasos: " + stepsCount);
    }

    private void updateDistance() {
        distanceCovered = stepsCount * 0.7f;
        roundedistance = Math.round(distanceCovered * 100.0) / 100.0;

        distanceTextView.setText("Distancia recorrida: " + roundedistance + " metros");
    }

    private void updateCalories() {
        caloriesBurned = stepsCount * CALORIES_PER_STEP;

         roundedCalories = Math.round(caloriesBurned * 100.0) / 100.0;



        caloriesTextView.setText("Calorías quemadas: " + roundedCalories);
    }

    public void Guardar(){
    }

    private void getFirstLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            latitudFinal = location.getLatitude();
                            longitudFinal = location.getLongitude();

                            LatLng posicionFinal = new LatLng(latitudFinal,longitudFinal);


                            MarkerOptions markerOptionsF = new MarkerOptions().position(posicionFinal).title("Comenzaste Aquí");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicionFinal,17));

                            googleMap.addMarker(markerOptionsF).showInfoWindow();
                        }
                    });

                }
            }
        });
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) ){

                                }
                                else
                                {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            1);
                                }

                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) ){

                                }
                                else
                                {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            1);
                                }


                                return;
                            }
                            map.setMyLocationEnabled(true);
                            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                                @Override
                                public void onMyLocationChange(Location location) {

                                    if(actualPosition)
                                    {
                                        latitudOrigen = location.getLatitude();
                                        longitudOrigen = location.getLongitude();
                                        actualPosition = true;

                                        LatLng miPosicion = new LatLng(latitudOrigen,longitudOrigen);

                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(new LatLng(latitudOrigen,longitudOrigen))
                                                .zoom(17)
                                                .bearing(1)
                                                .build();
                                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    }
                                }
                            });
                        }
                    });

                }
            }
        });
    }


    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            latitudFinal = location.getLatitude();
                            longitudFinal = location.getLongitude();

                            LatLng posicionFinal = new LatLng(latitudFinal,longitudFinal);
                            MarkerOptions markerOptionsL = new MarkerOptions().position(posicionFinal).title("Terminaste Aquí");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicionFinal,17));

                            googleMap.addMarker(markerOptionsL).showInfoWindow();
                        }
                    });

                }
            }
        });
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this.getContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }
    private void resetChronometro() {
        chronometro.setBase(SystemClock.elapsedRealtime());
        detenerse=0;
    }

    private void stopChronometro() {
        if (correr){
            chronometro.stop();
            detenerse = SystemClock.elapsedRealtime() - chronometro.getBase();
            correr=false;
        }
    }

    private void startChronometro() {
        if(!correr){
            chronometro.setBase(SystemClock.elapsedRealtime() - detenerse);
            chronometro.start();
            correr=true;
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==  REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }else{
                Toast.makeText(this.getContext(),"Permiso Denegado",Toast.LENGTH_LONG).show();
            }
        }
    }

}