package com.example.runnerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityRecuperar extends AppCompatActivity {

    EditText email;
    Button reset;
    private String mail = "";
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar);
        mAuth = FirebaseAuth.getInstance();

        mDialog = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.TxtrecuperaEmail);
        reset = (Button) findViewById(R.id.btnsendEmail);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mail = email.getText().toString();


                if(!mail.isEmpty()){
                    mDialog.setMessage("Espere un momento por favor...");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetPass();
                }else{
                    Toast.makeText(getApplicationContext(), "Debe ingresar el Email", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void resetPass(){
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Se ha enviado un correo para restablecer Contraseña", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(), "No se pudo enviar el Email para restablecer Contraseña", Toast.LENGTH_SHORT).show();
                }
                mDialog.dismiss();
            }
        });
    }

}