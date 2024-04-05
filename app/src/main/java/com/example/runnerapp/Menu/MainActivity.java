package com.example.runnerapp.Menu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.runnerapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements MapasFragment.OnFragmentInteractionListener {
    private BottomNavigationView bnvmenu;
    private Fragment fragment;
    private FragmentManager manager;

    TextView emailuser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initValues();
        initListener();


        emailuser = (TextView) findViewById(R.id.EmailUser);

        Bundle mail = this.getIntent().getExtras();
        String user = mail.getString("mail");
    }
    private void initView(){
        bnvmenu=findViewById(R.id.bnvmenu);

    }
    private void initValues(){
        manager =getSupportFragmentManager();
        loadFristFragment();
    }
    private void initListener(){
        bnvmenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
                int idmenu=item.getItemId();
                switch (idmenu){
                    case R.id.InicioFragment:
                        fragment = com.example.runnerapp.Menu.InicioFragment.newInstance();
                        openfragment(fragment);
                        return true;

                    case R.id.Infocarrera:
                        fragment =BlankFragment.newInstance();
                        openfragment(fragment);
                        return true;


                    case R.id.RutinaFragment:
                        fragment = CarreraFragment.newInstance();
                        openfragment(fragment);
                        return true;

                    case R.id.PerfilFragment:
                        fragment = PerfilFragment.newInstance();
                        openfragment(fragment);
                        return true;

                }
                return false;
            }
        });
    }

    private void openfragment(Fragment fragment){
        manager.beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();

    }

    private void loadFristFragment(){
        fragment = com.example.runnerapp.Menu.InicioFragment.newInstance();
        openfragment(fragment);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        
    }
}