package com.example.geotendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Accounts extends AppCompatActivity {

    // View Attributes
    TextView nameValTV, emailValTV, classValTV, phoneValTV;

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
        setContentView(R.layout.activity_accounts);

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
                    startActivity(new Intent(getApplicationContext(), Home.class).putExtra("Uid", getIntent().getStringExtra("Uid")));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.nav_account:
                    startActivity(new Intent(getApplicationContext(), Accounts.class)
                            .putExtra("latitude", getIntent().getStringExtra("latitude"))
                            .putExtra("longitude", getIntent().getStringExtra("longitude"))
                            .putExtra("Uid", getIntent().getStringExtra("Uid"))
                    );
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
                            Uri.parse("https://www.google.com/maps/search/?api=1&query="+getIntent().getStringExtra("latitude")+","+getIntent().getStringExtra("longitude"))));
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


        // Getting data from FireStore
        nameValTV = findViewById(R.id.nameVal);
        emailValTV = findViewById(R.id.emailVal);
        classValTV = findViewById(R.id.classVal);
        phoneValTV = findViewById(R.id.phoneVal);
        FirebaseFirestore.getInstance().collection("Users").document(getIntent().getStringExtra("Uid"))
                .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()) {
                    Map<String, Object> map = doc.getData();
                    nameValTV.setText(String.valueOf(map.get("FullName")));
                    emailValTV.setText(String.valueOf(map.get("EmailID")));
                    classValTV.setText(String.valueOf(map.get("Class")));
                    phoneValTV.setText(String.valueOf(map.get("PhoneNumber")));
                }
            }
        });
    }
}