package com.trek.pricer.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trek.pricer.R;

public class LoginActivity extends AppCompatActivity {
    EditText email_edittext,password_edittext;
    Button login_button;
    TextView signup_textview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_button=(Button)findViewById(R.id.login_button);
        email_edittext=(EditText)findViewById(R.id.email_editText);
        password_edittext=(EditText)findViewById(R.id.password_editText);
        signup_textview=(TextView)findViewById(R.id.signup_textView);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
