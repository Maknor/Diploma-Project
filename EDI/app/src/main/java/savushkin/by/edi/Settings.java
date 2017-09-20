package savushkin.by.edi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Settings extends PreferenceActivity {

    public static final String APP_PREFERENCES_Login = "Login";
    public static final String APP_PREFERENCES_Password = "Password";
    public static final String APP_PREFERENCES_URL = "URL";
    public static final String APP_PREFERENCES_ServerName = "ServerName";
    public static Boolean chekSettings = false;
    final String LOG_TAG = "myLogs";
    SharedPreferences.Editor editor;
    SharedPreferences mSettings;
    SharedPreferences pref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        mSettings = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //обрабатываем нажатие кнопки в настройках
        Preference button = (Preference)findPreference("pref_button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg) {
                pref.getString(getString(R.string.pref_login), "Login");
                pref.getString(getString(R.string.pref_password), "Password");
				//pref.getString(getString(R.string.pref_server), "ServerName");
				//pref.getString(getString(R.string.pref_url), "URL");
                editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_Login, pref.getString(getString(R.string.pref_login), "Login").toString());
                editor.putString(APP_PREFERENCES_Password, pref.getString(getString(R.string.pref_password), "Password").toString());
                editor.putString(APP_PREFERENCES_ServerName, pref.getString(getString(R.string.pref_server), "ServerName").toString());
                editor.putString(APP_PREFERENCES_URL, pref.getString(getString(R.string.pref_url), "URL").toString());
                editor.apply();
                Toast.makeText(Settings.this, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show();
                chekSettings = true;
                Log.d(LOG_TAG, "NewLog = " + mSettings.getString("Login", APP_PREFERENCES_Login));
                Log.d(LOG_TAG, "NewPass = " + mSettings.getString("Password", APP_PREFERENCES_Password));
                Log.d(LOG_TAG, "NewServerName = " + mSettings.getString("ServerName", APP_PREFERENCES_ServerName));
                Log.d(LOG_TAG, "NewURL = " + mSettings.getString("URL", APP_PREFERENCES_URL));

                return true;
            }
        });
    }
}
