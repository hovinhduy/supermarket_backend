package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi liên quan đến quản lý ảnh đơn vị sản phẩm
 */
public class ProductUnitImageException extends RuntimeException {

    public ProductUnitImageException(String message) {
        super(message);
    }

    public ProductUnitImageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception khi ProductUnit không tồn tại
     */
    public static class ProductUnitNotFoundException extends ProductUnitImageException {
        public ProductUnitNotFoundException(Long productUnitId) {
            super("Không tìm thấy đơn vị sản phẩm với ID: " + productUnitId);
        }
    }

    /**
     * Exception khi ProductImage không tồn tại
     */
    public static class ProductImageNotFoundException extends ProductUnitImageException {
        public ProductImageNotFoundException(Integer productImageId) {
            super("Không tìm thấy ảnh sản phẩm với ID: " + productImageId);
        }
    }

    /**
     * Exception khi ảnh không thuộc về cùng sản phẩm với ProductUnit
     */
    public static class ImageProductMismatchException extends ProductUnitImageException {
        public ImageProductMismatchException(Integer imageId, Long productUnitId) {
            super(String.format("Ảnh %d không thuộc về cùng sản phẩm với đơn vị sản phẩm %d", imageId, productUnitId));
        }
    }

    /**
     * Exception khi ảnh đã được gán cho ProductUnit
     */
    public static class ImageAlreadyAssignedException extends ProductUnitImageException {
        public ImageAlreadyAssignedException(Integer imageId, Long productUnitId) {
            super(String.format("Ảnh %d đã được gán cho đơn vị sản phẩm %d", imageId, productUnitId));
        }
    }

    /**
     * Exception khi file upload không hợp lệ
     */
    public static class InvalidImageFileException extends ProductUnitImageException {
        public InvalidImageFileException(String reason) {
            super("File ảnh không hợp lệ: " + reason);
        }
    }

    /**
     * Exception khi không thể upload file
     */
    public static class ImageUploadFailedException extends ProductUnitImageException {
        public ImageUploadFailedException(String fileName, Throwable cause) {
            super("Không thể upload file ảnh: " + fileName, cause);
        }
    }

    /**
     * Exception khi không tìm thấy ảnh chính
     */
    public static class PrimaryImageNotFoundException extends ProductUnitImageException {
        public PrimaryImageNotFoundException(Long productUnitId) {
            super("Không tìm thấy ảnh chính cho đơn vị sản phẩm: " + productUnitId);
        }
    }

    /**
     * Exception khi cố gắng xóa ảnh chính duy nhất
     */
    public static class CannotRemovePrimaryImageException extends ProductUnitImageException {
        public CannotRemovePrimaryImageException() {
            super("Không thể xóa ảnh chính duy nhất. Vui lòng đặt ảnh khác làm ảnh chính trước khi xóa.");
        }
    }
}
