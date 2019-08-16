package com.sveder.cardboardpassthrough;

/**
 * Created by Toshiba on 05/05/2016.
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SignupActivity extends Activity {
    private final static String REGISTER_API_ENDPOINT_URL = "http://192.168.1.78:3000/api/v1/registrations";
    private SharedPreferences mPreferences;
    private String mUserEmail;
    private String mUserName;
    private String mUserLastname;
    private String mUser;

    private String mUserPassword;
    private String mUserPasswordConfirmation;

    private static final String TAG = "SignupActivity";

    @Bind(R.id.userName) EditText _nameText;
    @Bind(R.id.userEmail) EditText _emailText;
    @Bind(R.id.userPassword) EditText _passwordText;
    @Bind(R.id.btn_signup) Button _signupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

    }
    public void login(View button){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void signup(View button) {

        EditText userNameField = (EditText) findViewById(R.id.userName);
        mUserName = userNameField.getText().toString();

        EditText userLastNameField = (EditText) findViewById(R.id.userLastName);
        mUserLastname = userLastNameField.getText().toString();

        EditText userField = (EditText) findViewById(R.id.user);
        mUser = userField.getText().toString();

        EditText userEmailField = (EditText) findViewById(R.id.userEmail);
        mUserEmail = userEmailField.getText().toString();

        EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
        mUserPassword = userPasswordField.getText().toString();

        EditText userPasswordConfirmationField = (EditText) findViewById(R.id.userPasswordConfirmation);
        mUserPasswordConfirmation = userPasswordConfirmationField.getText().toString();

        if (mUserName.length() == 0 || mUserLastname.length() == 0 || mUser.length() == 0 ||  mUserEmail.length() == 0 || mUserPassword.length() == 0 || mUserPasswordConfirmation.length() == 0) {
            // input fields are empty
            CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_LONG);
            toast.show("Por favor complete todo los campos");
            return;
        } else {
            if (!mUserPassword.equals(mUserPasswordConfirmation)) {
                // password doesn't match confirmation
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_LONG);
                toast.show("Tus contrasenas no coinciden");
                return;
            } else if(mUserName.length() > 10 || mUserLastname.length() > 15 || mUser.length() > 10){
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_LONG);
                toast.show("Los campos de nombre, apellido o usuario no son correctos");
                return;
            }else if(mUserPassword.length() < 10 || mUserPasswordConfirmation.length() < 10 ){
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_LONG);
                toast.show("La contraseña debe de tener mas de 10 caracteres");
                return;
            }else {
                // everything is ok!
                RegisterTask registerTask = new RegisterTask(SignupActivity.this);
                registerTask.setMessageLoading("Registering new account...");
                registerTask.execute(REGISTER_API_ENDPOINT_URL);
            }
        }
        /*
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);*/
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
    private class RegisterTask extends UrlJsonAsyncTask {
        public RegisterTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // add the users's info to the post params
                    userObj.put("name", mUserName);
                    userObj.put("lastname", mUser);
                    userObj.put("username", mUser);
                    userObj.put("email", mUserEmail);
                    userObj.put("password", mUserPassword);
                    userObj.put("password_confirmation", mUserPasswordConfirmation);
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
                    editor.commit();

                    // launch the HomeActivity and close this one
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}
