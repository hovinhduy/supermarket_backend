package iuh.fit.supermarket.service;

import com.google.zxing.BarcodeFormat;

/**
 * Service interface cho việc tạo barcode
 */
public interface BarcodeService {

    /**
     * Tạo barcode dưới dạng byte array (PNG image)
     * 
     * @param code mã barcode cần tạo
     * @param format định dạng barcode (EAN_13, CODE_128, QR_CODE, etc.)
     * @return byte array của ảnh barcode (PNG format)
     */
    byte[] generateBarcode(String code, BarcodeFormat format);

    /**
     * Tạo barcode với định dạng mặc định (EAN_13)
     * 
     * @param code mã barcode cần tạo
     * @return byte array của ảnh barcode (PNG format)
     */
    byte[] generateBarcode(String code);

    /**
     * Tạo barcode và upload lên S3, trả về URL
     * 
     * @param code mã barcode cần tạo
     * @param format định dạng barcode
     * @param folder thư mục trên S3
     * @return URL của ảnh barcode trên S3
     */
    String generateBarcodeUrl(String code, BarcodeFormat format, String folder);

    /**
     * Tạo barcode với định dạng mặc định và upload lên S3
     * 
     * @param code mã barcode cần tạo
     * @param folder thư mục trên S3
     * @return URL của ảnh barcode trên S3
     */
    String generateBarcodeUrl(String code, String folder);

    /**
     * Tạo mã barcode ngẫu nhiên theo định dạng EAN-13
     * 
     * @return mã EAN-13 (13 chữ số)
     */
    String generateRandomEAN13();
}
