package com.nextgen.hasnatfyp;



import android.app.ProgressDialog;
import android.content.Context;

public class LoadingDialogHelper {

    private ProgressDialog progressDialog;

    public LoadingDialogHelper(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
    }

    public void showLoadingDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
