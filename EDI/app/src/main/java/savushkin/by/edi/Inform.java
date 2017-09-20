package savushkin.by.edi;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Inform extends AppCompatActivity {
    public static final String APP_PREFERENCES_Login = "Login";
    public static final String APP_PREFERENCES_Password = "Password";
    public static final String APP_PREFERENCES_URL = "URL";
    public static final String APP_PREFERENCES_ServerName = "ServerName";
    final String LOG_TAG = "myLogs";
    SharedPreferences.Editor editor;
    SharedPreferences mSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        mSettings = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
        editor = mSettings.edit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(!item.isChecked()) item.setChecked(true);
        switch (id) {
            case R.id.action_start:
                Toast.makeText(Inform.this, getString(R.string.action_start2), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Главная");
                Intent intent3 = new Intent(Inform.this, EDI.class);
                intent3.putExtra("Login", mSettings.getString("Login", APP_PREFERENCES_Login));
                intent3.putExtra("Password", mSettings.getString("Password", APP_PREFERENCES_Password));
    //          intent3.putExtra("URL", mSettings.getString("URL", APP_PREFERENCES_URL));
    //          intent3.putExtra("ServerName", mSettings.getString("ServerName", APP_PREFERENCES_ServerName));
                startActivity(intent3);
                this.finish();
                break;
            case R.id.action_settings:
                Toast.makeText(Inform.this, getString(R.string.action_settings), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Настройки");
                Intent intent2 = new Intent(Inform.this, Settings.class);
                startActivity(intent2);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
