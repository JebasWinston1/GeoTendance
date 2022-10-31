package com.example.geotendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    boolean valid;
    Button loginButton;
    EditText emailIDET;
    EditText passwordET;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailIDET = findViewById(R.id.emailID);
        passwordET = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.loginButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        loginButton.setOnClickListener(view -> {
            checkFields(emailIDET);
            checkFields(passwordET);
            fAuth.signInWithEmailAndPassword(emailIDET.getText().toString(), passwordET.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "LoggedIn Successfully!", Toast.LENGTH_LONG).show();
                        checkUserAccessLevel(authResult.getUser().getUid());
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, "Try again, your credentials are not valid!", Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void checkUserAccessLevel(String uid) {
        DocumentReference dRef = fStore.collection("Users").document(uid);
        dRef.get().addOnSuccessListener(documentSnapshot -> {
            Log.d("TAG", "onSuccess " + documentSnapshot.getData());
            if(documentSnapshot.getString("isAdmin") != null) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
            } else {
                startActivity(new Intent(getApplicationContext(), Registeration.class));
                finish();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        });

    }

    public boolean checkFields(EditText textField) {
        if(textField.getText().toString().isEmpty()) {
            valid = false;
            textField.setError("Enter a value!");
        } else {
            valid = true;
        }
        return valid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }
    }
}