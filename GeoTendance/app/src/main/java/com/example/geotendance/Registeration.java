package com.example.geotendance;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.github.drjacky.imagepicker.ImagePicker;
//import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Map;

public class Registeration extends AppCompatActivity {

//    CircularImageView userImage;
//    FloatingActionButton userImageButton;
    boolean valid = true;
    EditText fullNameET, emailIDET, passwordET, classET;
    Button submitButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        fullNameET = (EditText) findViewById(R.id.fullName);
        emailIDET = (EditText) findViewById(R.id.emailID);
        passwordET = (EditText) findViewById(R.id.password);
        classET = (EditText) findViewById(R.id.department);
        submitButton = (Button) findViewById(R.id.submit);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        submitButton.setOnClickListener(view -> {
            if(valid) {
                checkFields(fullNameET);
                checkFields(emailIDET);
                checkFields(passwordET);
                checkFields(classET);
                fAuth.createUserWithEmailAndPassword(emailIDET.getText().toString(), passwordET.getText().toString())
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = fAuth.getCurrentUser();
                            DocumentReference dReference = fStore.collection("Users").document(user.getUid());
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("FullName", fullNameET.getText().toString());
                            userInfo.put("EmailID", emailIDET.getText().toString());
                            userInfo.put("Class", classET.getText().toString());
                            userInfo.put("isUser", "1");
                            dReference.set(userInfo);
                            Toast.makeText(this, "User Added Successfully!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, Home.class));
                            finish();
                        })
                        .addOnFailureListener(exception -> {
                            Toast.makeText(this, "Action Failed, Try Again!", Toast.LENGTH_LONG).show();
                        });
            }
        });
//        userImage = (CircularImageView) findViewById(R.id.userImage);
//        userImageButton = (FloatingActionButton) findViewById(R.id.userImageButton);
//        userImageButton.setOnClickListener(view -> {
//            ImagePicker.with(this)
//                    .crop()
//                    .maxResultSize(1024, 1024, true)
//                    .bothCameraGallery();
//        });
    }

    public boolean checkFields(EditText textField) {
        if(textField.getText().toString().isEmpty()) {
            valid = false;
            textField.setError("Field is empty");
        } else {
            valid = true;
        }
        return valid;
    }












//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 101 && resultCode == Activity.RESULT_OK) {
//            Uri uri = data.getData();
//            userImage.setImageURI(uri);
//        } else {
//            Toast.makeText(getApplicationContext(),"No Image Selected", Toast.LENGTH_LONG).show();
//        }
//    }
}