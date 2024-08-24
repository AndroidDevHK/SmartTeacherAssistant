package com.nextgen.hasnatfyp;



import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

public class CustomLoadingDialog extends Dialog {

    private String animationFileName;
    private String title;

    public CustomLoadingDialog(@NonNull Context context, String animationFileName, String title) {
        super(context);
        this.animationFileName = animationFileName;
        this.title = title;
        setCancelable(false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_loading_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CardView cardViewLoading = findViewById(R.id.cardView_loading);
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        TextView loadingText = findViewById(R.id.loading_text);

        loadingAnimation.setAnimation(animationFileName);
        loadingText.setText(title);

    }
}

