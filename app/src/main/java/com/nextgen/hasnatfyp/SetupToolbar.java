package com.nextgen.hasnatfyp;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SetupToolbar {

    public static void setup(Activity activity, Toolbar toolbar, String title, boolean showBackButton) {
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(showBackButton);
            if (showBackButton) {
                toolbar.setNavigationOnClickListener(view -> activity.onBackPressed());
            }
        }
    }

    public static void setMenu(Toolbar toolbar, @MenuRes int menuResId, Toolbar.OnMenuItemClickListener listener) {
        toolbar.inflateMenu(menuResId);
        toolbar.setOnMenuItemClickListener(listener);

        Menu menu = toolbar.getMenu();
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setOnMenuItemClickListener((MenuItem.OnMenuItemClickListener) listener);
            }
        }
    }
}
