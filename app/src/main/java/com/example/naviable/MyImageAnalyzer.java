package com.example.naviable;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
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

            BarcodeScanner scanner = BarcodeScanning.getClient();
            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            readBarcodeData(barcodes);
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
        Barcode.UrlBookmark url;
        for (Barcode barcode : barcodes) {
            switch(barcode.getValueType()) {
                case Barcode.TYPE_URL:
                    // code block
                    url = barcode.getUrl();
                    break;
                default:
                    // code block
            }
        }
    }
}
