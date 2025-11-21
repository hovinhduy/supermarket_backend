package iuh.fit.supermarket.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface cho việc upload file lên AWS S3
 */
public interface S3FileUploadService {

    /**
     * Upload file lên S3 bucket
     * 
     * @param file   file cần upload
     * @param folder thư mục trong S3 bucket
     * @return URL của file đã upload
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Xóa file từ S3 bucket
     * 
     * @param fileUrl URL của file cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    boolean deleteFile(String fileUrl);

    /**
     * Kiểm tra file có hợp lệ không (định dạng, kích thước)
     * 
     * @param file file cần kiểm tra
     * @return true nếu file hợp lệ, false nếu không hợp lệ
     */
    boolean isValidFile(MultipartFile file);

    /**
     * Tạo tên file duy nhất
     * 
     * @param originalFilename tên file gốc
     * @return tên file duy nhất
     */
    String generateUniqueFileName(String originalFilename);

    /**
     * Upload byte array lên S3 bucket
     * 
     * @param fileBytes byte array của file
     * @param fileName tên file
     * @param contentType loại nội dung (ví dụ: "image/png")
     * @param folder thư mục trong S3 bucket
     * @return URL của file đã upload
     */
    String uploadFile(byte[] fileBytes, String fileName, String contentType, String folder);
}

