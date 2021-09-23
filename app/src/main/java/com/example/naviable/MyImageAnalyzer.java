package com.example.naviable;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

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
    @Override
    public void analyze(@NonNull ImageProxy image) {
        scanBarcode(image);
    }
    @SuppressLint("UnsafeExperimentalUsageError")
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
                    });
        }
    }

    private void readBarcodeData(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            Log.i("QR Scanner:", "readBarcodeData: " + barcode.getRawValue());
        }
    }
}
