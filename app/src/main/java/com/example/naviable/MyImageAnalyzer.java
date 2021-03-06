package com.example.naviable;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.location.GnssAntennaInfo;
import android.media.Image;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.naviable.activities.CodeScannerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;

public class MyImageAnalyzer implements ImageAnalysis.Analyzer {

    private QrListener qrListener;

    public MyImageAnalyzer(QrListener listener){
        this.qrListener = listener;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        scanBarcode(image);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanBarcode(ImageProxy imageProxy) {
         Image inputImage = imageProxy.getImage();
        if (inputImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(inputImage, imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE
                    ).build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);
            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            if (barcodes != null && barcodes.size() > 0) {
                                readBarcodeData(barcodes);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            imageProxy.close();
                        }
                    });
        }
    }

    private void readBarcodeData(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            Log.i("QR Scanner:", "readBarcodeData: " + barcode.getRawValue());
            qrListener.onDataLoaded(barcode.getRawValue());
        }
    }
}
