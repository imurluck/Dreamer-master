package com.example.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by yourgod on 2017/7/30.
 */

public class DialogUtil {

    public static ProgressDialog progressDialog;


    public static void showProgressDialog(Context context, String message) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
