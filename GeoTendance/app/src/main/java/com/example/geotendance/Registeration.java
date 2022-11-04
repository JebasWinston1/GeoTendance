package com.example.geotendance;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Registeration extends AppCompatActivity {

    // View Attributes
    CircularImageView userImage;
    FloatingActionButton userImageButton;
    EditText fullNameET, emailIDET, passwordET, classET, phoneNumberET;
    Button submitButton;

    // Firebase Attributes
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseStorage fStorage;
    StorageReference storageRef;

    // Other Attributes
    boolean valid = true;
    Uri imageUri;

    // Hamburger menu Attributes
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        // Hamburger menu
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.menu_open, R.string.menu_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_home:
                    startActivity(new Intent(this, Home.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude",getIntent().getStringExtra("longitude"))
                            .putExtra("Uid", getIntent().getStringExtra("Uid")));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_account:
                    startActivity(new Intent(this, Accounts.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude",getIntent().getStringExtra("longitude"))
                            .putExtra("Uid", getIntent().getStringExtra("Uid")));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_location:
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query="+
                                    getIntent().getStringExtra("latitude")+","+getIntent().getStringExtra("longitude"))));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;
            }
            return false;
        });
        fullNameET = (EditText) findViewById(R.id.fullName);
        emailIDET = (EditText) findViewById(R.id.emailID);
        passwordET = (EditText) findViewById(R.id.password);
        classET = (EditText) findViewById(R.id.department);
        phoneNumberET = findViewById(R.id.phoneNumber);
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
                            userInfo.put("PhoneNumber", phoneNumberET.getText().toString());
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

        // UI Functionalities
        userImage = (CircularImageView) findViewById(R.id.userImage);
        userImageButton = (FloatingActionButton) findViewById(R.id.userImageButton);
        userImageButton.setOnClickListener(view -> {
            choosePicture();
        });

        // Firebase Functionalities
        fStorage = FirebaseStorage.getInstance();
        storageRef = fStorage.getReference();
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

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            userImage.setImageURI(imageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        Uri file = Uri.fromFile(new File("path/to/images/"));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();
        String randomKey = UUID.randomUUID().toString();
        StorageReference mountainsRef = storageRef.child("images/"+randomKey  );
        mountainsRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Snackbar.make(findViewById(android.R.id.content), "Image Uploaded.", Snackbar.LENGTH_LONG).show();
                    pd.dismiss();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "upload Failed", Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    pd.setMessage("Progress: " + (int) progressPercent + "%");
                });
    }
}