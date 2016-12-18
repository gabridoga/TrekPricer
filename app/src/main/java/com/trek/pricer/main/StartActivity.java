package com.trek.pricer.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.trek.pricer.R;

public class StartActivity extends AppCompatActivity {
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        auth=FirebaseAuth.getInstance();
        getUserSession();

    }
    public void getUserSession(){


        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent=new Intent(StartActivity.this,MainActivity.class);
            startActivity(intent);
        }
        else {

            Intent intent=new Intent(StartActivity.this,LoginActivity.class);
            startActivity(intent);

        }

    }
}
