package com.trek.pricer.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.trek.pricer.R;

public class LoginActivity extends AppCompatActivity {
    EditText email_edittext,password_edittext;
    Button login_button;
    TextView signup_textview;
    String email, password;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        login_button=(Button)findViewById(R.id.login_button);
        email_edittext=(EditText)findViewById(R.id.email_editText);
        password_edittext=(EditText)findViewById(R.id.password_editText);
        signup_textview=(TextView)findViewById(R.id.signup_textView);
        signup_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();

            }
        });
    }

    ProgressDialog progressDialog;
    private void login()
    {
        if (!isvalidate()) return;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Getting user data from server..");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful())
                {
                    AuthResult result = task.getResult();
//                    Toast.makeText(getApplicationContext(), result.getUser().getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

            }
        });
    }

    boolean isvalidate()
    {
        email = email_edittext.getText().toString().trim();
        password = password_edittext.getText().toString().trim();
        if (email.equals("")){
            email_edittext.setError("Please email address");
            return false;
        }
        if (password.equals("")){
            password_edittext.setError("Please enter password");
            return false;}
        if (password.length()<6){
            password_edittext.setError("Password length should be 6 more");
            return false;}
        return true;
    }
}
