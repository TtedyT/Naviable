package com.example.naviable.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.naviable.DB;
import com.example.naviable.MyImageAnalyzer;
import com.example.naviable.NaviableApplication;
import com.example.naviable.QrListener;
import com.example.naviable.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.dmoral.toasty.Toasty;

public class CodeScannerActivity extends AppCompatActivity {
    private final int REQUEST_CODE_PERMISSIONS = 10;
    private final String REQUIRED_PERMISSION = Manifest.permission.CAMERA;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private NaviableApplication app;
    private boolean alterShownFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanner);
        app = NaviableApplication.getInstance();

        if (checkCameraPermission())
            useCamera();

        ImageButton backButton = findViewById(R.id.back_button_qr);
        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    void useCamera() {
        Log.i("CodeScannerActivity", "useCamera: ");
        previewView = findViewById(R.id.preview_view);
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        ArrayList<String> locations = new ArrayList<String>(app.getDB().getLocations());
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .build();

        Toast invalidQrToast = Toasty.info(this,
                "Invalid QR code. Location does not exist.",
                Toast.LENGTH_SHORT, true);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        MyImageAnalyzer analyzer = new MyImageAnalyzer(new QrListener() {
            @Override
            public void onDataLoaded(String data) {
                // If collected data from QR code in locations list, display alert
                if (locations.contains(data)) {
                    if (!alterShownFlag) {
                        alterShownFlag = true;
                        cameraProvider.unbindAll();
                        alertDialogBuilder.setTitle("Scan current location")
                                .setMessage(String.format("Is %s your current location?", data))
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        app.setSearchSource(data);
                                        finish();
                                        alterShownFlag = false;
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        cameraProvider.bindToLifecycle(CodeScannerActivity.this,
                                                cameraSelector,
                                                imageAnalysis,
                                                preview
                                        );
                                        alterShownFlag = false;
                                    }
                                })
                                .create()
                                .show();
                    }
                } else {
                    // Prevent toasts from "stacking" by scanning multiple QR's
                    if (!invalidQrToast.getView().isShown())
                        invalidQrToast.show();
                }
            }
        });

        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        cameraProvider.bindToLifecycle(this,
                cameraSelector,
                imageAnalysis,
                preview
        );
    }

    public boolean checkCameraPermission() {
        Log.i("CodeScannerActivity", "checkCameraPermission: ");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(this)
                        .setTitle("Camera Permission")
                        .setMessage("The app needs permission to access the camera.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CodeScannerActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        REQUEST_CODE_PERMISSIONS);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_PERMISSIONS);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        useCamera();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

}