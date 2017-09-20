package savushkin.by.edi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class StartActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String APP_PREFERENCES_Login = "Login";
    public static final String APP_PREFERENCES_Password = "Password";
    public static final String APP_PREFERENCES_URL = "URL";
    public static final String APP_PREFERENCES_ServerName = "ServerName";
    public static final String APP_Cheking = "ChBox";


    Button bLogin;
    EditText editLogin, editPassword, editURL, editServerName;
    CheckBox cbRemember;
    SharedPreferences.Editor editor;
    SharedPreferences mSettings;
    boolean checkFlag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        editLogin = (EditText) findViewById(R.id.edit_user);
        editPassword = (EditText) findViewById(R.id.edit_password);
        editURL = (EditText) findViewById(R.id.edit_url);
        editServerName = (EditText) findViewById(R.id.edit_name);
        bLogin = (Button) findViewById(R.id.button_login);
        cbRemember = (CheckBox) findViewById(R.id.cbRemember);
        cbRemember.setOnCheckedChangeListener(this);
        checkFlag = cbRemember.isChecked();
        bLogin.setOnClickListener(this);

        mSettings = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
        editor = mSettings.edit();
        if(mSettings.contains(APP_Cheking)) {
            cbRemember.setChecked(mSettings.getBoolean(APP_Cheking, false));
        }//Загрузка последнего сохраненного значения чекбокса, если последнего нет то ставится false

        if (checkFlag) {
            Intent intent = new Intent(StartActivity.this, EDI.class);
            intent.putExtra("Login", mSettings.getString("Login", APP_PREFERENCES_Login));
            intent.putExtra("Password", mSettings.getString("Password", APP_PREFERENCES_Password));
//          intent.putExtra("URL", mSettings.getString("URL", APP_PREFERENCES_URL));
//          intent.putExtra("ServerName", mSettings.getString("ServerName", APP_PREFERENCES_ServerName));
            startActivity(intent);
            this.finish();
        }
        if ( !internetStatus() ){
            Toast.makeText(getApplicationContext(),
                    "Нет соединения с интернетом!",Toast.LENGTH_LONG).show();
            return;
        }
    }
    @Override
    public void onClick(View v) {

        if (checkFlag) {
            editor.putString(APP_PREFERENCES_Login, editLogin.getText().toString());
            editor.putString(APP_PREFERENCES_Password, editPassword.getText().toString());
//            editor.putString(APP_PREFERENCES_URL, editURL.getText().toString());
//            editor.putString(APP_PREFERENCES_ServerName, editServerName.getText().toString());
            editor.putBoolean(APP_Cheking, cbRemember.isChecked());
            editor.apply();
        }
        if (!internetStatus()) {
            Toast.makeText(getApplicationContext(),
                    "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (editLogin.length() < 1 || editPassword.length() < 1) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(StartActivity.this, EDI.class);
                intent.putExtra("Login", editLogin.getText().toString());
                intent.putExtra("Password", editPassword.getText().toString());
//          intent.putExtra("URL", editURL.getText().toString());
//          intent.putExtra("ServerName", editServerName.getText().toString());
                startActivity(intent);
                this.finish();
            }
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        checkFlag = isChecked;
    }
        public boolean internetStatus() {
            String cs = Context.CONNECTIVITY_SERVICE;
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(cs);
            if (cm.getActiveNetworkInfo() == null) {
                return false;
            } else {
                return true;
            }
        }
}