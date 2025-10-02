package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.dto.unit.UnitDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi thông tin đơn vị sản phẩm
 */
@Schema(description = "Thông tin đơn vị sản phẩm")
public record ProductUnitResponse(

                /**
                 * ID đơn vị sản phẩm
                 */
                @Schema(description = "ID đơn vị sản phẩm", example = "1") Long id,

                /**
                 * Mã đơn vị sản phẩm
                 */
                @Schema(description = "Mã đơn vị sản phẩm", example = "COCA_CHAI_001") String code,

                /**
                 * Mã vạch
                 */
                @Schema(description = "Mã vạch của đơn vị sản phẩm", example = "1234567890123") String barcode,

                /**
                 * Tỷ lệ quy đổi so với đơn vị cơ bản
                 */
                @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1") Integer conversionValue,

                /**
                 * Có phải là đơn vị cơ bản không
                 */
                @Schema(description = "Có phải là đơn vị cơ bản không", example = "true") Boolean isBaseUnit,

                /**
                 * Trạng thái hoạt động
                 */
                @Schema(description = "Trạng thái hoạt động", example = "true") Boolean isActive,

                /**
                 * Thông tin đơn vị tính
                 */
                @Schema(description = "Thông tin đơn vị tính") UnitDto unit,

                /**
                 * ID sản phẩm
                 */
                @Schema(description = "ID sản phẩm", example = "1") Long productId,

                /**
                 * Danh sách hình ảnh của đơn vị sản phẩm
                 */
                @Schema(description = "Danh sách hình ảnh của đơn vị sản phẩm") List<ProductUnitImageDto> images,

                /**
                 * Thời gian tạo
                 */
                @Schema(description = "Thời gian tạo") LocalDateTime createdAt,

                /**
                 * Thời gian cập nhật
                 */
                @Schema(description = "Thời gian cập nhật") LocalDateTime updatedAt) {
}
