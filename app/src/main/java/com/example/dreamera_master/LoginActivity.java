package com.example.dreamera_master;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private Button loginIn;

    private EditText passwordEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginIn = (Button) findViewById(R.id.login_login_in);
        passwordEdit = (EditText) findViewById(R.id.login_password_edit);
        loginIn();
    }

    private void loginIn() {
        loginIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEdit.getText().toString();
                if (password.equals("123456")) {
                    finish();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (password.equals("")) {
                    Snackbar.make(v, "Please input password!", Snackbar.LENGTH_SHORT)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .show();
                } else if (!password.equals("123456")) {
                    Snackbar.make(v, "password is wrong", Snackbar.LENGTH_SHORT)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .show();
                }
            }
        });
    }
}
