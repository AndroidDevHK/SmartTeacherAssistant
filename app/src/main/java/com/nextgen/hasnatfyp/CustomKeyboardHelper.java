package com.nextgen.hasnatfyp;
import android.app.Activity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class CustomKeyboardHelper {
    private View customKeyboard;
    private EditText activeEditText;
    private Activity activity;

    public CustomKeyboardHelper(Activity activity, int keyboardViewId) {
        this.activity = activity;
        this.customKeyboard = activity.findViewById(keyboardViewId);
        if (customKeyboard == null) {
            Log.e("CustomKeyboardHelper", "Custom keyboard view not found");
        }
        initKeys();
    }

    private void initKeys() {
        activity.findViewById(R.id.key1).setOnClickListener(v -> handleKeyPress("1"));
        activity.findViewById(R.id.key2).setOnClickListener(v -> handleKeyPress("2"));
        activity.findViewById(R.id.key3).setOnClickListener(v -> handleKeyPress("3"));
        activity.findViewById(R.id.key4).setOnClickListener(v -> handleKeyPress("4"));
        activity.findViewById(R.id.key5).setOnClickListener(v -> handleKeyPress("5"));
        activity.findViewById(R.id.key6).setOnClickListener(v -> handleKeyPress("6"));
        activity.findViewById(R.id.key7).setOnClickListener(v -> handleKeyPress("7"));
        activity.findViewById(R.id.key8).setOnClickListener(v -> handleKeyPress("8"));
        activity.findViewById(R.id.key9).setOnClickListener(v -> handleKeyPress("9"));
        activity.findViewById(R.id.key0).setOnClickListener(v -> handleKeyPress("0"));
        activity.findViewById(R.id.keyA).setOnClickListener(v -> handleKeyPress("A"));
        activity.findViewById(R.id.keyBackspace).setOnClickListener(v -> handleBackspace());
    }

    private void handleKeyPress(String keyValue) {
        if (activeEditText != null && activeEditText.isFocused()) {
            int cursorPos = activeEditText.getSelectionStart();
            activeEditText.getText().insert(cursorPos, keyValue);
        }
    }

    private void handleBackspace() {
        if (activeEditText != null && activeEditText.isFocused()) {
            int cursorPos = activeEditText.getSelectionStart();
            if (cursorPos > 0 && activeEditText.getText().length() > 0) {
                activeEditText.getText().delete(cursorPos - 1, cursorPos);
            }
        }
    }

    public void setActiveEditText(EditText editText) {
        Log.d("CustomKeyboardHelper", "setActiveEditText called");
        if (this.activeEditText != null) {
            this.activeEditText.clearFocus();
        }
        this.activeEditText = editText;
        this.activeEditText.setInputType(InputType.TYPE_NULL); // Disable default keyboard
        this.activeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard();
            } else {
                hideKeyboard();
            }
        });
        this.activeEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setActiveEditText((EditText) v);
                showKeyboard();
            }
            return true;
        });
    }

    public void showKeyboard() {
        if (customKeyboard != null) {
            customKeyboard.setVisibility(View.VISIBLE);
        } else {
            Log.e("CustomKeyboardHelper", "Custom keyboard is null");
        }
    }

    public void hideKeyboard() {
        if (customKeyboard != null) {
            customKeyboard.setVisibility(View.GONE);
        } else {
            Log.e("CustomKeyboardHelper", "Custom keyboard is null");
        }
    }
}
