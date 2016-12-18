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

public class SignupActivity extends AppCompatActivity {
    EditText email_edittext,password_edittext;
    TextView already_signuptextview;
    Button singup_button;
    String emailstring,passwordstaring;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email_edittext=(EditText)findViewById(R.id.signup_emaileditText);
        password_edittext=(EditText)findViewById(R.id.signup_passworkeditText);
        already_signuptextview=(TextView)findViewById(R.id.alreadysignup_textView);
        singup_button=(Button)findViewById(R.id.signup_button);
        mAuth=FirebaseAuth.getInstance();
        singup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        already_signuptextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    ProgressDialog progressDialog;
    private void signup()
    {
        if (!isvalidate()) return;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating user data from server..");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(emailstring, passwordstaring)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }

    boolean isvalidate()
    {
        emailstring = email_edittext.getText().toString().trim();
        passwordstaring = password_edittext.getText().toString().trim();
        if (emailstring.equals("")){
            email_edittext.setError("Please email address");
            return false;
        }
        if (passwordstaring.equals("")){
            password_edittext.setError("Please enter password");
            return false;}
        if (passwordstaring.length()<6){
            password_edittext.setError("Password length should be 6 more");
            return false;}
        return true;
    }


}
