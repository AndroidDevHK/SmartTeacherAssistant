package com.nextgen.hasnatfyp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

public class SimpleLoadingDialog extends Dialog {

    private String title;

    public SimpleLoadingDialog(@NonNull Context context, String title) {
        super(context);
        this.title = title;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.simple_custom_loading_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Calculate dialog width to occupy 80% of the screen
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.7); // Adjusted width to 70% of screen width
        getWindow().setAttributes(layoutParams);

        // Make the dialog not cancelable
        setCancelable(false);

        // Find views
        CardView cardViewLoading = findViewById(R.id.cardView_loading);
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        TextView loadingText = findViewById(R.id.loading_text);

        // Set animation
        loadingAnimation.setAnimation("loading.json");

        // Set text
        loadingText.setText(title);

        // Reduce size of animation
        ViewGroup.LayoutParams animationLayoutParams = loadingAnimation.getLayoutParams();
        int animationSize = (int) context.getResources().getDimension(R.dimen.animation_size); // Define the desired size in dimensions
        animationLayoutParams.width = animationSize;
        animationLayoutParams.height = animationSize;
        loadingAnimation.setLayoutParams(animationLayoutParams);

        // Adjust padding and margins to reduce overall size
        int padding = (int) context.getResources().getDimension(R.dimen.dialog_padding) / 2; // Reduced padding
        cardViewLoading.setPadding(padding, padding, padding, padding);
        ViewGroup.MarginLayoutParams cardViewParams = (ViewGroup.MarginLayoutParams) cardViewLoading.getLayoutParams();
        cardViewParams.setMargins(padding, padding, padding, padding);
        cardViewLoading.setLayoutParams(cardViewParams);
    }
}
