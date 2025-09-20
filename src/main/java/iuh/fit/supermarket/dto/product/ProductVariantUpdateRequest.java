package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho yêu cầu cập nhật biến thể sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật biến thể sản phẩm")
public class ProductVariantUpdateRequest {

    /**
     * Tên biến thể
     */
    @Schema(description = "Tên biến thể", example = "Áo Thun Polo - Đỏ - L - Cái")
    private String variantName;

    /**
     * Mã vạch
     */
    @Schema(description = "Mã vạch của biến thể", example = "1234567890123")
    private String barcode;

    /**
     * Số lượng tồn kho (deprecated - chỉ để tương thích, sẽ được bỏ qua)
     */
    @Deprecated
    @Schema(description = "Số lượng tồn kho hiện tại (deprecated - sử dụng WarehouseService)", example = "100")
    private BigDecimal quantityOnHand;

    /**
     * Số lượng đã đặt trước (deprecated - chỉ để tương thích, sẽ được bỏ qua)
     */
    @Deprecated
    @Schema(description = "Số lượng đã được đặt trước (deprecated - sử dụng WarehouseService)", example = "10")
    private BigDecimal quantityReserved;

    /**
     * Số lượng tối thiểu cảnh báo (deprecated - chỉ để tương thích, sẽ được bỏ qua)
     */
    @Deprecated
    @Schema(description = "Số lượng tối thiểu để cảnh báo hết hàng (deprecated - sử dụng WarehouseService)", example = "20")
    private BigDecimal minQuantity;

    /**
     * Cho phép bán hay không
     */
    @Schema(description = "Trạng thái cho phép bán", example = "true")
    private Boolean allowsSale;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động của biến thể", example = "true")
    private Boolean isActive;

    /**
     * ID đơn vị mới (nếu muốn thay đổi đơn vị)
     */
    @Schema(description = "ID đơn vị mới", example = "1")
    private Long unitId;

    /**
     * Danh sách ID các giá trị thuộc tính mới (nếu muốn thay đổi thuộc tính)
     */
    @Schema(description = "Danh sách ID các giá trị thuộc tính mới", example = "[1, 2, 3]")
    private List<Long> attributeValueIds;
}
