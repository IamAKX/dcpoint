package com.akashapplications.dcpoint;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akashapplications.dcpoint.utils.API;
import com.akashapplications.dcpoint.utils.RequestQueueSingleton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.marozzi.roundbutton.RoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    RoundButton registerBtn;
    TextView loginTV;
    EditText name, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale("de");
        setContentView(R.layout.activity_register);

        setStatusBarGradiant(this);

        RelativeLayout background = findViewById(R.id.register_background);
        AnimationDrawable animationDrawable = (AnimationDrawable) background.getBackground();
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        registerBtn = findViewById(R.id.registerBtn);
        loginTV = findViewById(R.id.loginTV);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    registerBtn.startAnimation();
                    new RegisterCall(email.getText().toString(), name.getText().toString(), password.getText().toString()).execute();
                    email.setText("");
                    name.setText("");
                    password.setText("");
                }
            }
        });



        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), Login.class));
                finish();
            }
        });

        CheckBox checkBox = findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b)
                    password.setTransformationMethod(new PasswordTransformationMethod());
                else
                    password.setTransformationMethod(null);

                password.setSelection(password.getText().toString().length());
            }
        });

    }

    private boolean validateInputs() {
        if (name.getText().toString().length() == 0 || email.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast1), Toast.LENGTH_SHORT).show();
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (!pat.matcher(email.getText().toString()).matches()) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast2), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.getText().toString().length() < 8) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast3), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_1);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    private class RegisterCall extends AsyncTask<Void, Void, Void> {
        String email, name, password;

        public RegisterCall(String email, String name, String password) {
            this.email = email;
            this.name = name;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            try {
                params.put("email", email);
                params.put("name", name);
                params.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API.REGISTER, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {


                            try {
                                if (response.has("msg"))
                                    Toast.makeText(getBaseContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getBaseContext(), response.getString("Success"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            registerBtn.revertAnimation();
                            startActivity(new Intent(getBaseContext(), Login.class));
                            finish();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    String data = new String(networkResponse.data);

                    Log.e("checking",data);
                    registerBtn.revertAnimation();
                    try {
                        JSONObject response = new JSONObject(data);
                        if (response.has("msg"))
                            Toast.makeText(getBaseContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getBaseContext(), response.getString("Failed"), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            jsonObjectRequest.setShouldCache(false);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            RequestQueue requestQueue = RequestQueueSingleton.getInstance(getBaseContext())
                    .getRequestQueue();
            requestQueue.getCache().clear();
            requestQueue.add(jsonObjectRequest);
            return null;
        }
    }
    public void setLocale(String localeName) {

        Locale myLocale = new Locale(localeName);
        Locale.setDefault(myLocale);

        Configuration configuration = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(myLocale);
        }
        else
            configuration.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

    }
}
