package com.sse.iamhere;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class ScannerActivity extends AppCompatActivity {
    private boolean detected = false;
    private boolean isProcessing = false;
    private int preventBackLimit = 3;

    private QRCodeReaderView qrCodeReaderView;
    private AlertDialog processDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null) {
            isProcessing = savedInstanceState.getBoolean("isProcessing");
            preventBackLimit = savedInstanceState.getInt("preventBackLimit");
        }

        init();
    }

    private void init() {
        setContentView(R.layout.activity_scanner);

        qrCodeReaderView = findViewById(R.id.scanner_QRCodeReaderView);
        qrCodeReaderView.setOnQRCodeReadListener((text, points) -> {
            if (!detected) {
                detected=true;
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  // <<--- Depreciated, refers to this,
                v.vibrate(200);
                qrCodeReaderView.stopCamera();
                submitQRCode(text);
            }
        });

        qrCodeReaderView.setOnClickListener(v -> qrCodeReaderView.forceAutoFocus());

        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);
        // Use this function to change the auto focus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);
        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();

        ImageView returnIv = findViewById(R.id.scanner_returnIv);
        returnIv.setOnClickListener(v -> onBackPressed());
    }

    private void submitQRCode(String qrCodeStr) {
        showProcessingDialog();
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isProcessing = false;

                    if (failed && failCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                        showInfoToast(getString(R.string.splash_connectionTv_label), 5000);
                    }

                    showCompleteDialog(!failed);
                }
            });
        rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
        rb.callRequest(() -> { rb.attendeeSubmitQRCode(qrCodeStr); });
    }


    private void showProcessingDialog() {
        View view = View.inflate(this, R.layout.dialog_scanner_processing, null);

        processDialog = new AlertDialog.Builder(ScannerActivity.this)
                .setView(view)
                .show();
    }

    private void showCompleteDialog(boolean success) {
        View view = View.inflate(this, R.layout.dialog_scanner_complete, null);

        AlertDialog completeDialog = new AlertDialog.Builder(ScannerActivity.this).create();
        completeDialog.setOnDismissListener(dialog -> {
            if (!success) {
                qrCodeReaderView.startCamera();
                detected = false;
            } else {
                finishActivity(true);
            }
        });

        view.findViewById(R.id.scanner_complete_retryBtn).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                completeDialog.dismiss();
            }
        });
        view.findViewById(R.id.scanner_complete_returnBtn).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                finishActivity(true);
            }
        });

        if (!success) {
            ViewCompat.setBackgroundTintList(
                    view.findViewById(R.id.scanner_complete_stateTv),
                    ColorStateList.valueOf(getColor(R.color.red_error)));

            view.findViewById(R.id.scanner_complete_retryBtn).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.scanner_complete_stateTv)).setText(getString(R.string.scanner_complete_stateTv_fail));
        }

        completeDialog.setView(view);
        completeDialog.show();

        if (processDialog!=null) {
            processDialog.hide();
        }
    }

    private void finishActivity(boolean updateNeeded) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updateNeeded", updateNeeded);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void showInfoToast(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Toast.makeText(this, msg, duration).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (qrCodeReaderView!=null && !isProcessing) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (qrCodeReaderView!=null) {
            qrCodeReaderView.stopCamera();
        }
    }

    @Override
    public void onBackPressed() {
        if (isProcessing && preventBackLimit > 0) {
            showInfoToast(getString(R.string.scanner_return_msg),Toast.LENGTH_LONG);
            preventBackLimit--;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("isProcessing", isProcessing);
        outState.putInt("preventBackLimit", preventBackLimit);
    }
}
