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
import com.akashapplications.dcpoint.utils.LocalPreference;
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

import java.net.PasswordAuthentication;
import java.util.Locale;

public class Login extends AppCompatActivity {
    RoundButton loginBtn;
    TextView registerTV;
    EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale("de");
        setContentView(R.layout.activity_login);

        setStatusBarGradiant(this);

        RelativeLayout background = findViewById(R.id.login_background);
        AnimationDrawable animationDrawable = (AnimationDrawable) background.getBackground();
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        loginBtn = findViewById(R.id.loginBtn);
        registerTV = findViewById(R.id.registerTV);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    loginBtn.startAnimation();

                    new LoginCall(email.getText().toString(), password.getText().toString()).execute();
                    email.setText("");
                    password.setText("");
                }
            }
        });

        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), Register.class));
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

    private boolean validateInput() {
        if (email.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast1), Toast.LENGTH_SHORT).show();
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

    private class LoginCall extends AsyncTask<Void, Void, Void> {
        String email, password;

        public LoginCall(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            try {
                params.put("email", email);
                params.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API.LOGIN, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.has("msg"))
                                    Toast.makeText(getBaseContext(), getResources().getString(R.string.welcome), Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getBaseContext(), getResources().getString(R.string.welcome), Toast.LENGTH_LONG).show();

                                if(response.has("user"))
                                {
                                    response = response.getJSONObject("user");
                                    LocalPreference localPreference = new LocalPreference(getBaseContext());
                                    if(response.has("email"))
                                        localPreference.setEmail(response.getString("email"));

                                    if(response.has("name"))
                                        localPreference.setName(response.getString("name"));

                                    localPreference.setLoggedIn(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            loginBtn.revertAnimation();

                            startActivity(new Intent(getBaseContext(), Home.class));
                            finish();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    String data = new String(networkResponse.data);

                    Log.e("checking",data);
                    loginBtn.revertAnimation();
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
