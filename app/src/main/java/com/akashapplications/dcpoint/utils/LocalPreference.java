package com.akashapplications.dcpoint.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class LocalPreference {

    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public LocalPreference(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("Local_Preference", Activity.MODE_PRIVATE);
    }

    public boolean setLoggedIn(boolean status)
    {
        editor = preferences.edit();
        editor.putBoolean("loggedin",status);
        return editor.commit();
    }

    public boolean idLoggedIn()
    {
        return preferences.getBoolean("loggedin",false);
    }

    public boolean setName(String name)
    {
        editor = preferences.edit();
        editor.putString("name",name);
        return editor.commit();
    }

    public String getName()
    {
        return preferences.getString("name", null);
    }

    public boolean setEmail(String email)
    {
        editor = preferences.edit();
        editor.putString("email",email);
        return editor.commit();
    }

    public String getEmail()
    {
        return preferences.getString("email", null);
    }

    public boolean setWelle(int welle)
    {
        editor = preferences.edit();
        editor.putInt("welle",welle);
        return editor.commit();
    }

    public int getWelle()
    {
        return preferences.getInt("welle", 0);
    }


    public boolean setKFZ(String kfz)
    {
        editor = preferences.edit();
        editor.putString("kfz",kfz);
        return editor.commit();
    }

    public String getKFZ()
    {
        return preferences.getString("kfz", null);
    }


    public boolean setTour(String tour)
    {
        editor = preferences.edit();
        editor.putString("tour",tour);
        return editor.commit();
    }

    public String getTour()
    {
        return preferences.getString("tour", null);
    }


    public boolean setLanguage(String language)
    {
        editor = preferences.edit();
        editor.putString("language",language);
        return editor.commit();
    }

    public String getLanguage()
    {
        return preferences.getString("language", "de");
    }


    public boolean setScanner(String scanner)
    {
        editor = preferences.edit();
        editor.putString("scanner",scanner);
        return editor.commit();
    }

    public String getScanner()
    {
        return preferences.getString("scanner", null);
    }

    public void logOut()
    {
        editor = preferences.edit();
        editor.clear().commit();
    }
}
