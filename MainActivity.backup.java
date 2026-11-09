package com.nova.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;

public class MainActivity extends Activity {

    private LinearLayout messages;
    private EditText input;
    private ScrollView scroll;

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

        // HEADER
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

        TextView status = text("● ONLINE", 11,
            Color.rgb(72, 220, 155), true);
        header.addView(status);

        root.addView(header);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(28, 42, 62));
        root.addView(line,
            new LinearLayout.LayoutParams(-1, dp(1)));

        // CHAT AREA
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(16), dp(20), dp(16), dp(20));

        scroll.addView(messages);

        root.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        addMessage(
            "Halo! Saya NOVA. ✨\n\nSaya siap membantu kamu. Apa yang ingin kita lakukan hari ini?",
            false
        );

        // COMPOSER
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
        input.setBackground(round(
            Color.rgb(29, 43, 64), 22));

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

        ((InputMethodManager) getSystemService(
            Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(input.getWindowToken(), 0);

        // Temporary prototype response.
        addMessage(
            "Pesan diterima! ✨\n\nIntegrasi AI NOVA akan diaktifkan pada tahap berikutnya.",
            false
        );
    }

    private void addMessage(String value, boolean user) {

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
            user ? Color.rgb(26, 69, 111) : PANEL,
            18
        ));

        LinearLayout.LayoutParams bubbleParams =
            new LinearLayout.LayoutParams(-2, -2);
        bubbleParams.topMargin = dp(6);
        bubbleParams.bottomMargin = dp(18);
        bubbleParams.width = -2;

        wrapper.addView(label);
        wrapper.addView(bubble, bubbleParams);

        messages.addView(wrapper);

        scroll.post(() ->
            scroll.fullScroll(View.FOCUS_DOWN));
    }

    private TextView text(
        String value, int size, int color, boolean bold) {

        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT,
                Typeface.BOLD);
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
}
