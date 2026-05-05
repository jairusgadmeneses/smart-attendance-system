package org.jairus.qcu_attendance;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;

public class QRGenerator {

    public static void main(String[] args) {

        File folder = new File("QRCodes");
        folder.mkdir();

        for (int i = 1; i <= 10; i++) {
            try {

                String studentID = String.format("STU-%03d", i);

                QRCodeWriter writer = new QRCodeWriter();

                BitMatrix matrix = writer.encode(studentID, BarcodeFormat.QR_CODE, 1000, 1000);

                File imageFile = new File("QRCodes/" + studentID + ".png");
                MatrixToImageWriter.writeToPath(matrix, "PNG", imageFile.toPath());

                System.out.println("Done! " + studentID + ".png");

            } catch (Exception e) {
                System.out.println("Error making QR code for " + i);
            }
        }
    }
}