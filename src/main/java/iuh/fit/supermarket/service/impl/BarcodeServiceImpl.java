package iuh.fit.supermarket.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import iuh.fit.supermarket.service.BarcodeService;
import iuh.fit.supermarket.service.S3FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Implementation cho BarcodeService sử dụng ZXing library
 */
@Service
@Slf4j
public class BarcodeServiceImpl implements BarcodeService {

    @Autowired
    private S3FileUploadService s3FileUploadService;

    private static final int BARCODE_WIDTH = 300;
    private static final int BARCODE_HEIGHT = 150;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Tạo barcode dưới dạng byte array
     */
    @Override
    public byte[] generateBarcode(String code, BarcodeFormat format) {
        log.info("Tạo barcode với mã: {} và định dạng: {}", code, format);

        try {
            // Cấu hình hints cho barcode
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1); // Giảm margin xung quanh barcode

            // Tạo BitMatrix
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(code, format, BARCODE_WIDTH, BARCODE_HEIGHT, hints);

            // Chuyển BitMatrix thành ảnh PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

            byte[] barcodeBytes = outputStream.toByteArray();
            log.info("Tạo barcode thành công, size: {} bytes", barcodeBytes.length);

            return barcodeBytes;

        } catch (Exception e) {
            log.error("Lỗi khi tạo barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo barcode: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo barcode với định dạng mặc định (EAN_13)
     */
    @Override
    public byte[] generateBarcode(String code) {
        return generateBarcode(code, BarcodeFormat.CODE_128);
    }

    /**
     * Tạo barcode và upload lên S3
     */
    @Override
    public String generateBarcodeUrl(String code, BarcodeFormat format, String folder) {
        log.info("Tạo barcode và upload lên S3: {}", code);

        try {
            // Tạo barcode
            byte[] barcodeBytes = generateBarcode(code, format);

            // Tạo tên file unique
            String fileName = s3FileUploadService.generateUniqueFileName("barcode_" + code + ".png");

            // Upload lên S3
            String barcodeUrl = s3FileUploadService.uploadFile(barcodeBytes, fileName, "image/png", folder);

            log.info("Upload barcode lên S3 thành công: {}", barcodeUrl);
            return barcodeUrl;

        } catch (Exception e) {
            log.error("Lỗi khi tạo và upload barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo và upload barcode: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo barcode với định dạng mặc định và upload lên S3
     */
    @Override
    public String generateBarcodeUrl(String code, String folder) {
        return generateBarcodeUrl(code, BarcodeFormat.CODE_128, folder);
    }

    /**
     * Tạo mã barcode ngẫu nhiên theo định dạng EAN-13
     * EAN-13 bao gồm 13 chữ số:
     * - 12 chữ số đầu: mã sản phẩm
     * - 1 chữ số cuối: checksum (tính toán theo thuật toán EAN-13)
     */
    @Override
    public String generateRandomEAN13() {
        Random random = new Random();
        
        // Tạo 12 chữ số đầu
        StringBuilder ean13 = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            ean13.append(random.nextInt(10));
        }

        // Tính checksum
        int checksum = calculateEAN13Checksum(ean13.toString());
        ean13.append(checksum);

        return ean13.toString();
    }

    /**
     * Tính checksum cho mã EAN-13
     */
    private int calculateEAN13Checksum(String code) {
        int sum = 0;
        for (int i = 0; i < code.length(); i++) {
            int digit = Character.getNumericValue(code.charAt(i));
            // Nhân với 1 nếu vị trí chẵn (index lẻ vì bắt đầu từ 0), nhân với 3 nếu vị trí lẻ
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : (10 - remainder);
    }
}
