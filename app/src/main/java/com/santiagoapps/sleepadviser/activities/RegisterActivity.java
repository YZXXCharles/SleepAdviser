package com.santiagoapps.sleepadviser.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.santiagoapps.sleepadviser.R;
import com.santiagoapps.sleepadviser.data.repo.UserRepo;
import com.santiagoapps.sleepadviser.helpers.DBHelper;
import com.santiagoapps.sleepadviser.data.model.User;

public class RegisterActivity extends AppCompatActivity {

    private Button btnEnter;
    private EditText txtName;
    private EditText txtEmail;
    private EditText txtPassword,txtPassword2;
    private TextView txtView_Signin;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference database;
    private DBHelper myDb;
    private final static String TAG = "Dormie (" + RegisterActivity.class.getSimpleName() + ") ";

    String gender, name, occupation, sleep;
    int age;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();

    }

    public void init(){
        btnEnter = (Button)findViewById(R.id.btnEnter);
        txtName = (EditText)findViewById(R.id.txtName);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtPassword2 = (EditText)findViewById(R.id.txtPassword2);
        txtView_Signin = (TextView) findViewById(R.id.txtSignin);

        myDb = new DBHelper(this);
        database = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(RegisterActivity.this, NavigationActivity.class));
                }
            }
        };


        Intent intent = getIntent();
        if(intent.getExtras() != null){
            Bundle bd = intent.getExtras();
            gender = (String) bd.get("SESSION_GENDER");
            name = (String) bd.get("SESSION_NAME");
            occupation = (String) bd.get("SESSION_OCCUPATION");
            age = (int) bd.get("SESSION_AGE");
            sleep = (String) bd.get("SESSION_SLEEP_GOAL");
            txtName.setText(name);
        }

    }

    protected void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void registerUser(){
        //User Inputs
        final String name = txtName.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String password2 = txtPassword2.getText().toString().trim();

        //ProgressBar
        final ProgressDialog mDialog = new ProgressDialog(RegisterActivity.this);
        mDialog.setMessage("Please wait...");
        Log.d(TAG, "Checking log-in credentials");

        //Error handling
        if (TextUtils.isEmpty(name)){
            txtName.setError("This field is required.");
            txtName.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(email)){
            txtEmail.setError("This field is required.");
            txtEmail.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password)){
            txtPassword.setError("Please enter password");
            txtPassword.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password2)){
            txtPassword2.setError("Please enter confirm password");
            txtPassword2.requestFocus();
            return;
        }

        if(!password.equalsIgnoreCase(password2)){
            txtPassword.setError("Password doesn't match!");
            txtPassword.requestFocus();
            return;
        }
        //Start progress dialog
        mDialog.show();

        //Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mDialog.dismiss();

                            //save to Database
                            saveToDatabase(name,email,password);

                        }else{
                            Toast.makeText(RegisterActivity.this, "Email already exist!", Toast.LENGTH_SHORT).show();
                            txtEmail.setError("Email already exist!");
                            txtEmail.requestFocus();
                            mDialog.dismiss();
                        }
                    }
                }); 
    }

    public void saveToDatabase(String name, String email, String password){
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        final User userObject;

        //firebase is connected
        if (user != null) {
            UserProfileChangeRequest profileUpdates =new UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build();
            user.updateProfile(profileUpdates);

            userObject = new User(user.getUid(), email, password, name);
            Log.d(TAG, "Current user is: " + user.getUid());
        }

        else{
            userObject = new User(email, password, name);
            Log.d(TAG, "User is null");
        }

        //save to firebase
        database.child(user.getUid()).setValue(userObject)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Success! Welcome, " + userObject.getName());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed! Check database handling");
                    }
                });

        //save to local database
        UserRepo userRepo = new UserRepo();
        long id = userRepo.registerUser(userObject);

        if(id!=-1){
            startActivity(new Intent(RegisterActivity.this, NavigationActivity.class));
        }


        SharedPreferences ref = getApplicationContext().getSharedPreferences("Dormie", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString("email", email);

    }

    public void onClick_signin(View v){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void onClick_btnEnter(View v){
        registerUser();
    }


}
