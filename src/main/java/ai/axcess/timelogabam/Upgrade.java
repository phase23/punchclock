package ai.axcess.timelogabam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Upgrade extends AppCompatActivity {

    private String apkUrl = "https://punchclock.ai/apk/app-latest.apk";
    private String apkFileName = "app-latest.apk";

    private String versionJsonUrl = "https://punchclock.ai/apk/aaaaversion.json";
    Button main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        checkVersionAndUpdate();

        main = findViewById(R.id.mainscreen);

        main.setOnClickListener(v -> {


            Intent intent = new Intent(Upgrade.this, MainActivity.class);
            startActivity(intent);
            ((Activity) Upgrade.this).finish();

        });
    }


    private void checkVersionAndUpdate() {
        new Thread(() -> {
            try {
                URL url = new URL(versionJsonUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                StringBuilder jsonText = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) {
                    jsonText.append((char) c);
                }

                JSONObject json = new JSONObject(jsonText.toString());
                int latestVersion = json.getInt("latest_version_code");
                String apkUrl = json.getString("apk_url");

                int currentVersion = getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionCode;

                if (latestVersion > currentVersion) {
                    runOnUiThread(() -> Toast.makeText(this, "New version available: " + latestVersion, Toast.LENGTH_SHORT).show());
                    downloadAndPromptInstall(apkUrl);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "App is up to date", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Version check failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }


    private void downloadAndPromptInstall(String apkUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                File apkFile = new File(getExternalFilesDir(null), "update.apk");
                InputStream input = new BufferedInputStream(url.openStream());
                FileOutputStream output = new FileOutputStream(apkFile);

                byte[] data = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                runOnUiThread(() -> promptInstall(apkFile));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void promptInstall(File file) {
        Uri apkUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}