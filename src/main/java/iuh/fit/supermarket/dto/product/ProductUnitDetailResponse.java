package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thông tin chi tiết đầy đủ của ProductUnit
 * Bao gồm thông tin sản phẩm, đơn vị, tồn kho và giá hiện tại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin chi tiết đầy đủ của ProductUnit")
public class ProductUnitDetailResponse {

    /**
     * ID đơn vị sản phẩm
     */
    @Schema(description = "ID đơn vị sản phẩm", example = "1")
    private Long productUnitId;

    /**
     * Mã vạch
     */
    @Schema(description = "Mã vạch của đơn vị sản phẩm", example = "1234567890123")
    private String barcode;

    /**
     * Tỷ lệ quy đổi so với đơn vị cơ bản
     */
    @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1")
    private Integer conversionValue;

    /**
     * Có phải là đơn vị cơ bản không
     */
    @Schema(description = "Có phải là đơn vị cơ bản không", example = "true")
    private Boolean isBaseUnit;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    /**
     * ID sản phẩm
     */
    @Schema(description = "ID sản phẩm", example = "1")
    private Long productId;

    /**
     * Tên sản phẩm
     */
    @Schema(description = "Tên sản phẩm", example = "Coca Cola")
    private String productName;

    /**
     * Mã sản phẩm
     */
    @Schema(description = "Mã sản phẩm", example = "COCA001")
    private String productCode;

    /**
     * ID đơn vị tính
     */
    @Schema(description = "ID đơn vị tính", example = "1")
    private Long unitId;

    /**
     * Tên đơn vị tính
     */
    @Schema(description = "Tên đơn vị tính", example = "Chai")
    private String unitName;

    /**
     * Số lượng tồn kho
     */
    @Schema(description = "Số lượng tồn kho hiện tại", example = "100")
    private Integer quantityOnHand;

    /**
     * Giá hiện tại (từ bảng giá đang áp dụng)
     */
    @Schema(description = "Giá hiện tại", example = "15000")
    private BigDecimal currentPrice;

    /**
     * ID bảng giá hiện tại
     */
    @Schema(description = "ID bảng giá hiện tại", example = "1")
    private Long priceId;

    /**
     * Mã bảng giá hiện tại
     */
    @Schema(description = "Mã bảng giá hiện tại", example = "PRICE_2024_001")
    private String priceCode;

    /**
     * Tên bảng giá hiện tại
     */
    @Schema(description = "Tên bảng giá hiện tại", example = "Bảng giá tháng 1/2024")
    private String priceName;

    /**
     * Có tồn kho không
     */
    @Schema(description = "Có tồn kho không", example = "true")
    public Boolean hasStock() {
        return quantityOnHand != null && quantityOnHand > 0;
    }

    /**
     * Có giá hiện tại không
     */
    @Schema(description = "Có giá hiện tại không", example = "true")
    public Boolean hasCurrentPrice() {
        return currentPrice != null;
    }
}
