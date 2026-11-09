package com.nova.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String NOVA_API =
            "https://nova-ai-server.rofikaldianto13.workers.dev/";

    private LinearLayout messages;
    private EditText input;
    private ScrollView scroll;
    private TextView status;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable thinkingAnimation;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final int BG = Color.rgb(8, 13, 24);
    private final int PANEL = Color.rgb(17, 27, 44);
    private final int BLUE = Color.rgb(75, 174, 255);
    private final int WHITE = Color.rgb(240, 245, 255);
    private final int MUTED = Color.rgb(139, 157, 185);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(14), dp(20), dp(14));

        TextView logo = text("✦", 27, BLUE, true);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(12), 0, 0, 0);
        brand.addView(text("NOVA", 20, WHITE, true));
        brand.addView(text("YOUR PERSONAL AI", 10, MUTED, true));

        header.addView(logo);
        header.addView(brand);

        View spacer = new View(this);
        header.addView(spacer,
                new LinearLayout.LayoutParams(0, 1, 1));

        status = text("● ONLINE", 11,
                Color.rgb(72, 220, 155), true);
        header.addView(status);
        root.addView(header);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(28, 42, 62));
        root.addView(line,
                new LinearLayout.LayoutParams(-1, dp(1)));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(16), dp(20), dp(16), dp(20));
        scroll.addView(messages);

        root.addView(scroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        addMessage(
                "Halo! Saya NOVA. ✨\n\nSekarang saya terhubung dengan mesin AI NOVA. Apa yang ingin kita lakukan hari ini?",
                false
        );

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setPadding(dp(12), dp(12), dp(12), dp(12));
        composer.setBackgroundColor(PANEL);

        input = new EditText(this);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setHint("Tanyakan sesuatu kepada NOVA...");
        input.setTextSize(14);
        input.setSingleLine(false);
        input.setMaxLines(4);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(round(Color.rgb(29, 43, 64), 22));

        composer.addView(input,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView send = text("➤", 23, BLUE, true);
        send.setGravity(Gravity.CENTER);
        send.setPadding(dp(14), dp(8), dp(8), dp(8));
        send.setOnClickListener(v -> sendMessage());

        composer.addView(send);
        root.addView(composer);

        setContentView(root);
    }

    private void sendMessage() {
        String message = input.getText().toString().trim();

        if (message.isEmpty()) return;

        input.setText("");
        addMessage(message, true);

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(input.getWindowToken(), 0);

        status.setText("● THINKING");
        status.setTextColor(BLUE);

        TextView loading = addMessage("NOVA sedang berpikir
● ○ ○", false);
        startThinkingAnimation(loading);

        executor.execute(() -> {
            try {
                String answer = askNova(message);

                handler.post(() -> {
                    stopThinkingAnimation(loading);
                    addMessage(answer, false);
                    status.setText("● ONLINE");
                    status.setTextColor(Color.rgb(72, 220, 155));
                });

            } catch (Exception e) {
                handler.post(() -> {
                    stopThinkingAnimation(loading);
                    addMessage(
                            "Maaf, NOVA mengalami masalah:

" + e.getMessage(),
                            false
                    );
                    status.setText("● ERROR");
                    status.setTextColor(Color.RED);
                });
            }
        });
    }

    private void startThinkingAnimation(TextView loading) {
        final String[] frames = {
                "● ○ ○",
                "○ ● ○",
                "○ ○ ●",
                "○ ● ○"
        };

        thinkingAnimation = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (loading.getParent() == null) return;

                loading.setText(
                        "NOVA sedang berpikir
" + frames[index]
                );
                loading.setAlpha(index % 2 == 0 ? 1.0f : 0.72f);

                index = (index + 1) % frames.length;
                handler.postDelayed(this, 320);
            }
        };

        handler.post(thinkingAnimation);
    }

    private void stopThinkingAnimation(TextView loading) {
        if (thinkingAnimation != null) {
            handler.removeCallbacks(thinkingAnimation);
            thinkingAnimation = null;
        }

        loading.animate().cancel();
        loading.setAlpha(1.0f);

        // Hapus seluruh wadah pesan, bukan hanya teks bubble.
        if (loading.getParent() instanceof View) {
            View wrapper = (View) loading.getParent();

            if (wrapper.getParent() instanceof ViewGroup) {
                ((ViewGroup) wrapper.getParent()).removeView(wrapper);
            }
        }
    }

        private String askNova(String message) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(NOVA_API);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(90000);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8"
            );
            connection.setRequestProperty("Accept", "application/json");

            JSONObject body = new JSONObject();
            body.put("message", message);

            byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(data);
            }

            int code = connection.getResponseCode();

            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                throw new Exception("Server tidak memberikan respons.");
            }

            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            JSONObject response = new JSONObject(result.toString());

            if (code < 200 || code >= 300) {
                String detail = response.optString(
                        "detail",
                        response.optString("error", "HTTP " + code)
                );
                throw new Exception(detail);
            }

            String answer = response.optString("reply", "").trim();

            if (answer.isEmpty()) {
                throw new Exception("Server belum mengirim jawaban AI.");
            }

            return answer;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private TextView addMessage(String value, boolean user) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(user ? Gravity.RIGHT : Gravity.LEFT);

        TextView label = text(
                user ? "YOU" : "✦ NOVA",
                10,
                user ? BLUE : MUTED,
                true
        );

        TextView bubble = text(value, 14, WHITE, false);
        bubble.setPadding(dp(15), dp(12), dp(15), dp(12));
        bubble.setBackground(round(
                user ? Color.rgb(26, 69, 111) : PANEL, 18
        ));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-2, -2);
        params.topMargin = dp(6);
        params.bottomMargin = dp(18);

        wrapper.addView(label);
        wrapper.addView(bubble, params);
        messages.addView(wrapper);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));

        return bubble;
    }

    private TextView text(
            String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
