package com.resways.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import android.graphics.Bitmap;
import android.graphics.Color;
import com.resways.app.R;
import java.util.Random;

public class ReservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        TextView restaurantName = findViewById(R.id.restaurantName);
        TextView pinText = findViewById(R.id.pinText);
        Button backHomeBtn = findViewById(R.id.backHomeBtn);
        ImageView qrImage = findViewById(R.id.qrCodeImageView);

        String name = getIntent().getStringExtra("RESTAURANT_NAME");
        if (name != null) {
            restaurantName.setText(name);
        }

        // Use real PIN from Backend (or fallback)
        String pinString = getIntent().getStringExtra("PIN");
        String bagId = getIntent().getStringExtra("BAG_ID");
        if (pinString == null) {
            pinString = "0000";
        }
        if (bagId == null) {
            bagId = "0";
        }
        
        String combinedCode = bagId + "-" + pinString;
        pinText.setText(combinedCode);
        
        // Generate QR Code
        Bitmap qrBitmap = generateQRCode(combinedCode, 500, 500);
        
        qrImage.setImageBitmap(qrBitmap);

        backHomeBtn.setOnClickListener(v -> finish());
    }
    
    private Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
