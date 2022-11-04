package com.example.geotendance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class AttendanceDetails extends AppCompatActivity {

    // UI Attributes
    CircleImageView circleImageView;
    FloatingActionButton uploadBtn;
    Button submit;

    // Hamburger menu Attributes
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    // Firebase Attributes
    FirebaseStorage fStorage;
    StorageReference storageRef;

    // Other Attributes
    Uri imageUri;
    final double DESTINATION_LATITUDE = 9.5246967;
    final double DESTINATION_LONGITUDE = 77.8552909;

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
        setContentView(R.layout.activity_attendance_details);

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
                    startActivity(new Intent(getApplicationContext(), Home.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude", getIntent().getStringExtra("longitude"))
                            .putExtra("Uid", getIntent().getStringExtra("Uid")));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_account:
                    startActivity(new Intent(getApplicationContext(), Accounts.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude", getIntent().getStringExtra("longitude"))
                            .putExtra("Uid", getIntent().getStringExtra("Uid")));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_attendance:
                    startActivity(new Intent(getApplicationContext(), AttendanceDetails.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude", getIntent().getStringExtra("longitude"))
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
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;
            }
            return false;
        });

        // UI Functionalities
        circleImageView = findViewById(R.id.uploadImage);
        uploadBtn = findViewById(R.id.uploadImageBtn);
        uploadBtn.setOnClickListener(view -> {
            choosePicture();
        });
        submit = findViewById(R.id.button);
        submit.setOnClickListener(view -> {
            FirebaseFirestore.getInstance().collection("Users").document(getIntent().getStringExtra("Uid")).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        // number of km per degree = ~111km (111.32 in google maps, but range varies between 110.567km at the equator and 111.699km at the poles)
                        double kiloMeters = 0.1;
                        double coef = kiloMeters/111.32;
                        double oldLatitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
                        double newLatitude = DESTINATION_LATITUDE + coef;
                        // pi / 180 ~= 0.01745
                        double oldLongitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
                        double newLongitude = DESTINATION_LONGITUDE + coef / Math.cos(DESTINATION_LATITUDE * 0.01745);
                        if((oldLatitude <= newLatitude) && (oldLatitude >= DESTINATION_LATITUDE)
                                && (oldLongitude <= newLongitude) && (oldLongitude >= DESTINATION_LONGITUDE)) {
                            sendNotification();
                            sendSMS(String.valueOf(map.get("FullName")), String.valueOf(map.get("EmailID")),
                                    String.valueOf(map.get("Class")), String.valueOf(map.get("PhoneNumber")));
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Sorry! You're not within the range.");
                            builder.setCancelable(true);
                            builder.setPositiveButton("Okay", (dialog, which) -> {
                                finish();
                            });
                            builder.setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.cancel();
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }
                }
            });
        });

        // Firebase Functionalities
        fStorage = FirebaseStorage.getInstance();
        storageRef = fStorage.getReference();
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_baseline_message_24) //set icon for notification
                        .setContentTitle("GeoTendance") //set title of notification
                        .setContentText("Your entry is registered successfully!")//this is notification message
                        .setAutoCancel(true) // makes auto cancel of notification
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT); //set priority of notification
        Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notification message will get at NotificationView
        notificationIntent.putExtra("message", "This is a notification message");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void sendSMS(String fullName, String email, String claas, String mobileNumber) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            StringBuilder message = new StringBuilder();
            message.append("Name: " + fullName + "\n");
            message.append("Email ID: " + email + "\n");
            message.append("Class: " + claas + "\n");
            message.append("Your attendance marked successfully!");
            smsManager.sendTextMessage(mobileNumber,null, String.valueOf(message),null,null);
            Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_LONG).show();
        }
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
            circleImageView.setImageURI(imageUri);
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