package uk.ac.tees.aad.W9517102.Common;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    Context context;

    public SharedPreference(Context ctx) {
        context = ctx;
    }

    public void addLastUser(String email,String password,String type) {
        editor = context.getApplicationContext().getSharedPreferences("LastUser", MODE_PRIVATE).edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("type", type);
        editor.apply();
    }


    public String getLastUserEmail() {
        prefs = context.getApplicationContext().getSharedPreferences("LastUser", MODE_PRIVATE);
        return prefs.getString("email", "");
    }

    public String getLastUserPassword() {
        prefs = context.getApplicationContext().getSharedPreferences("LastUser", MODE_PRIVATE);
        return prefs.getString("password", "");
    }

    public String getLastUserType() {
        prefs = context.getApplicationContext().getSharedPreferences("LastUser", MODE_PRIVATE);
        return prefs.getString("type", "");
    }

}
