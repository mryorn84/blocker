package com.example.youtubeblocker;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView txt = new TextView(this);
        txt.setText("YouTube Blocker\n= بررسی و مسدود کردن یوتیوب");
        txt.setTextSize(18);
        txt.setGravity(Gravity.CENTER);
        layout.addView(txt);

        Button startBtn = new Button(this);
        startBtn.setText("Start blocking");
        startBtn.setOnClickListener(v -> {
            if (!hasUsageStatsPermission(this)) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                Toast.makeText(this, "لطفاً در صفحه باز شده اجازه 'Usage access' را به این برنامه بدهید، سپس دوباره برگردید و Start را بزنید.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent svc = new Intent(this, MonitorService.class);
            startForegroundService(svc);
            Toast.makeText(this, "سرویس شروع شد", Toast.LENGTH_SHORT).show();
        });
        layout.addView(startBtn);

        Button stopBtn = new Button(this);
        stopBtn.setText("Stop blocking");
        stopBtn.setOnClickListener(v -> {
            Intent svc = new Intent(this, MonitorService.class);
            stopService(svc);
            Toast.makeText(this, "سرویس متوقف شد", Toast.LENGTH_SHORT).show();
        });
        layout.addView(stopBtn);

        setContentView(layout);
    }

    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
