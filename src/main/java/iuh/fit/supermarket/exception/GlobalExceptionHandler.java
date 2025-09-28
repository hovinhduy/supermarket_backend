package iuh.fit.supermarket.exception;

import iuh.fit.supermarket.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler để xử lý tất cả exception trong ứng dụng
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Xử lý ngoại lệ liên quan đến danh mục
         */
        @ExceptionHandler(CategoryException.class)
        public ResponseEntity<ApiResponse<String>> handleCategoryException(
                        CategoryException ex) {

                log.warn("Category exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ liên quan đến sản phẩm
         */
        @ExceptionHandler(ProductException.class)
        public ResponseEntity<ApiResponse<String>> handleProductException(
                        ProductException ex) {

                log.warn("Product exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ liên quan đến khách hàng
         */
        @ExceptionHandler(CustomerException.class)
        public ResponseEntity<ApiResponse<String>> handleCustomerException(
                        CustomerException ex) {

                log.warn("Customer exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ khi không tìm thấy khách hàng
         */
        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ApiResponse<String>> handleCustomerNotFoundException(
                        CustomerNotFoundException ex) {

                log.warn("Customer not found: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ khi khách hàng đã tồn tại
         */
        @ExceptionHandler(DuplicateCustomerException.class)
        public ResponseEntity<ApiResponse<String>> handleDuplicateCustomerException(
                        DuplicateCustomerException ex) {

                log.warn("Duplicate customer: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ validation khách hàng
         */
        @ExceptionHandler(CustomerValidationException.class)
        public ResponseEntity<ApiResponse<String>> handleCustomerValidationException(
                        CustomerValidationException ex) {

                log.warn("Customer validation error: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ không tìm thấy sản phẩm
         */
        @ExceptionHandler(ProductNotFoundException.class)
        public ResponseEntity<ApiResponse<String>> handleProductNotFoundException(
                        ProductNotFoundException ex) {

                log.warn("Product not found exception: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ sản phẩm trùng lặp
         */
        @ExceptionHandler(DuplicateProductException.class)
        public ResponseEntity<ApiResponse<String>> handleDuplicateProductException(
                        DuplicateProductException ex) {

                log.warn("Duplicate product exception: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ chung liên quan đến bảng giá
         */
        @ExceptionHandler(PriceException.class)
        public ResponseEntity<ApiResponse<String>> handlePriceException(
                        PriceException ex) {

                log.warn("Price exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ validation bảng giá
         */
        @ExceptionHandler(PriceValidationException.class)
        public ResponseEntity<ApiResponse<String>> handlePriceValidationException(
                        PriceValidationException ex) {

                log.warn("Price validation exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ trùng mã bảng giá
         */
        @ExceptionHandler(DuplicatePriceCodeException.class)
        public ResponseEntity<ApiResponse<String>> handleDuplicatePriceCodeException(
                        DuplicatePriceCodeException ex) {

                log.warn("Duplicate price code exception: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ không tìm thấy bảng giá
         */
        @ExceptionHandler(PriceNotFoundException.class)
        public ResponseEntity<ApiResponse<String>> handlePriceNotFoundException(
                        PriceNotFoundException ex) {

                log.warn("Price not found exception: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ xung đột business logic bảng giá
         */
        @ExceptionHandler(PriceConflictException.class)
        public ResponseEntity<ApiResponse<String>> handlePriceConflictException(
                        PriceConflictException ex) {

                log.warn("Price conflict exception: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ImportException
         */
        @ExceptionHandler(ImportException.class)
        public ResponseEntity<ApiResponse<Object>> handleImportException(ImportException ex) {
                log.error("Import error: {}", ex.getMessage(), ex);

                ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý WarehouseException
         */
        @ExceptionHandler(WarehouseException.class)
        public ResponseEntity<ApiResponse<Object>> handleWarehouseException(WarehouseException ex) {
                log.error("Warehouse error: {}", ex.getMessage(), ex);

                ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý InsufficientStockException
         */
        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientStockException(
                        InsufficientStockException ex) {

                log.warn("Insufficient stock: {}", ex.getMessage());

                Map<String, Object> details = new HashMap<>();
                details.put("variantId", ex.getVariantId());
                details.put("requiredQuantity", ex.getRequiredQuantity());
                details.put("availableQuantity", ex.getAvailableQuantity());

                ApiResponse<Map<String, Object>> response = ApiResponse.error(ex.getMessage(), details);
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý DuplicateImportCodeException
         */
        @ExceptionHandler(DuplicateImportCodeException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateImportCodeException(
                        DuplicateImportCodeException ex) {

                log.warn("Duplicate import code: {}", ex.getMessage());

                Map<String, Object> details = new HashMap<>();
                details.put("importCode", ex.getImportCode());

                ApiResponse<Map<String, Object>> response = ApiResponse.error(ex.getMessage(), details);
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý ImportCodeOverflowException
         */
        @ExceptionHandler(ImportCodeOverflowException.class)
        public ResponseEntity<ApiResponse<Object>> handleImportCodeOverflowException(
                        ImportCodeOverflowException ex) {

                log.error("Import code overflow: {}", ex.getMessage());

                ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        /**
         * Xử lý DuplicateStocktakeCodeException
         */
        @ExceptionHandler(DuplicateStocktakeCodeException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateStocktakeCodeException(
                        DuplicateStocktakeCodeException ex) {

                log.warn("Duplicate stocktake code: {}", ex.getMessage());

                Map<String, Object> details = new HashMap<>();
                details.put("stocktakeCode", ex.getStocktakeCode());

                ApiResponse<Map<String, Object>> response = ApiResponse.error(ex.getMessage(), details);
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý StocktakeException
         */
        @ExceptionHandler(StocktakeException.class)
        public ResponseEntity<ApiResponse<Object>> handleStocktakeException(StocktakeException ex) {
                log.error("Stocktake error: {}", ex.getMessage(), ex);

                ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Xử lý StocktakeNotFoundException
         */
        @ExceptionHandler(StocktakeNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleStocktakeNotFoundException(StocktakeNotFoundException ex) {
                log.warn("Stocktake not found: {}", ex.getMessage());

                ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        /**
         * Xử lý ngoại lệ chung liên quan đến promotion
         */
        @ExceptionHandler(PromotionException.class)
        public ResponseEntity<ApiResponse<String>> handlePromotionException(
                        PromotionException ex) {

                log.warn("Promotion exception: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ không tìm thấy promotion
         */
        @ExceptionHandler(PromotionNotFoundException.class)
        public ResponseEntity<ApiResponse<String>> handlePromotionNotFoundException(
                        PromotionNotFoundException ex) {

                log.warn("Promotion not found: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ validation promotion
         */
        @ExceptionHandler(PromotionValidationException.class)
        public ResponseEntity<ApiResponse<String>> handlePromotionValidationException(
                        PromotionValidationException ex) {

                log.warn("Promotion validation error: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ xung đột promotion
         */
        @ExceptionHandler(PromotionConflictException.class)
        public ResponseEntity<ApiResponse<String>> handlePromotionConflictException(
                        PromotionConflictException ex) {

                log.warn("Promotion conflict: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý ngoại lệ promotion trùng lặp
         */
        @ExceptionHandler(DuplicatePromotionException.class)
        public ResponseEntity<ApiResponse<String>> handleDuplicatePromotionException(
                        DuplicatePromotionException ex) {

                log.warn("Duplicate promotion: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Xử lý validation errors
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                log.warn("Validation error: {}", errors);

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Dữ liệu không hợp lệ", errors));
        }

        /**
         * Xử lý authentication exceptions
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
                        AuthenticationException ex, WebRequest request) {

                log.warn("Authentication error: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Xác thực thất bại: " + ex.getMessage()));
        }

        /**
         * Xử lý bad credentials exception
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(
                        BadCredentialsException ex) {

                log.warn("Bad credentials: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Email hoặc mật khẩu không chính xác"));
        }

        /**
         * Xử lý disabled account exception
         */
        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiResponse<String>> handleDisabledException(
                        DisabledException ex) {

                log.warn("Account disabled: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error("Tài khoản đã bị khóa"));
        }

        /**
         * Xử lý user not found exception
         */
        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ApiResponse<String>> handleUsernameNotFoundException(
                        UsernameNotFoundException ex) {

                log.warn("User not found: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Email hoặc mật khẩu không chính xác"));
        }

        /**
         * Xử lý access denied exception
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(
                        AccessDeniedException ex) {

                log.warn("Access denied: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error("Bạn không có quyền truy cập chức năng này"));
        }

        /**
         * Xử lý illegal argument exception
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
                        IllegalArgumentException ex) {

                log.warn("Illegal argument: {}", ex.getMessage());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Tham số không hợp lệ: " + ex.getMessage()));
        }

        /**
         * Xử lý runtime exception
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<String>> handleRuntimeException(
                        RuntimeException ex) {

                log.error("Runtime exception: ", ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("Có lỗi xảy ra trong hệ thống"));
        }

        /**
         * Xử lý tất cả exception khác
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<String>> handleGlobalException(
                        Exception ex, WebRequest request) {

                log.error("Unexpected error: ", ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("Có lỗi không xác định xảy ra"));
        }
}
