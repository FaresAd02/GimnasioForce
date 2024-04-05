package com.example.runnerapp.Menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.runnerapp.ActivityEditarPerfil;
import com.example.runnerapp.Activity_login;
import com.example.runnerapp.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment
{
    View vista;
    Button btn_logout, btn_editar;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    public PerfilFragment()
    {
        
    }

    public static PerfilFragment newInstance()
    {
        Bundle args = new Bundle();

        PerfilFragment fragment = new PerfilFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_perfil, container, false);

        btn_logout = vista.findViewById(R.id.btnLogout);
        btn_editar = vista.findViewById(R.id.btnEditar);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(vista.getContext(), "Sesión Cerrada", Toast.LENGTH_SHORT).show();
                to_back();
            }
        });

        btn_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                String email = null;
                if (firebaseAuth.getCurrentUser() != null) {
                    email = firebaseAuth.getCurrentUser().getEmail();
                }

                if(email != null && !email.trim().isEmpty()) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuario");
                    userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String databaseUid = userSnapshot.getKey();
                                    Intent intent = new Intent(getActivity(), ActivityEditarPerfil.class);
                                    intent.putExtra("DATABASE_UID", databaseUid);
                                    startActivity(intent);
                                }
                            } else {
                                Toast.makeText(getActivity(), "No se encontró el usuario con ese email.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), "Error en la base de datos.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "No hay email de usuario autenticado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return vista;
    }

    private void to_back() {
        Intent i = new Intent(vista.getContext().getApplicationContext(), Activity_login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}