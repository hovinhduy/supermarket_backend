package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.price.*;
import iuh.fit.supermarket.enums.PriceType;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface cho quản lý bảng giá
 */
public interface PriceService {

    /**
     * Tạo bảng giá mới
     * 
     * @param request thông tin bảng giá cần tạo
     * @return thông tin bảng giá đã tạo
     */
    PriceResponse createPrice(PriceCreateRequest request);

    /**
     * Cập nhật bảng giá
     * 
     * @param priceId ID bảng giá cần cập nhật
     * @param request thông tin cập nhật
     * @return thông tin bảng giá đã cập nhật
     */
    PriceResponse updatePrice(Long priceId, PriceUpdateRequest request);

    /**
     * Lấy thông tin bảng giá theo ID
     * 
     * @param priceId ID bảng giá
     * @param includeDetails có bao gồm chi tiết giá không
     * @return thông tin bảng giá
     */
    PriceResponse getPriceById(Long priceId, boolean includeDetails);

    /**
     * Lấy thông tin bảng giá theo mã
     * 
     * @param priceCode mã bảng giá
     * @param includeDetails có bao gồm chi tiết giá không
     * @return thông tin bảng giá
     */
    PriceResponse getPriceByCode(String priceCode, boolean includeDetails);

    /**
     * Lấy danh sách bảng giá với phân trang và lọc
     * 
     * @param request yêu cầu phân trang và lọc
     * @return danh sách bảng giá
     */
    Page<PriceResponse> getPricesAdvanced(PricePageableRequest request);

    /**
     * Xóa bảng giá (chỉ cho phép xóa bảng giá UPCOMING hoặc PAUSED)
     * 
     * @param priceId ID bảng giá cần xóa
     */
    void deletePrice(Long priceId);

    /**
     * Cập nhật trạng thái bảng giá
     * 
     * @param priceId ID bảng giá
     * @param request yêu cầu cập nhật trạng thái
     * @return thông tin bảng giá đã cập nhật
     */
    PriceResponse updatePriceStatus(Long priceId, PriceStatusUpdateRequest request);

    /**
     * Kích hoạt bảng giá (chuyển từ UPCOMING/PAUSED sang CURRENT)
     * 
     * @param priceId ID bảng giá
     * @return thông tin bảng giá đã kích hoạt
     */
    PriceResponse activatePrice(Long priceId);

    /**
     * Tạm dừng bảng giá (chuyển từ CURRENT sang PAUSED)
     * 
     * @param priceId ID bảng giá
     * @return thông tin bảng giá đã tạm dừng
     */
    PriceResponse pausePrice(Long priceId);

    /**
     * Lấy danh sách bảng giá theo trạng thái
     * 
     * @param status trạng thái bảng giá
     * @return danh sách bảng giá
     */
    List<PriceResponse> getPricesByStatus(PriceType status);

    /**
     * Lấy bảng giá hiện tại đang áp dụng
     * 
     * @return danh sách bảng giá CURRENT
     */
    List<PriceResponse> getCurrentPrices();

    /**
     * Tự động cập nhật trạng thái bảng giá (được gọi bởi scheduled task)
     * - Chuyển UPCOMING sang CURRENT khi đến thời gian bắt đầu
     * - Chuyển CURRENT sang EXPIRED khi hết hạn
     */
    void autoUpdatePriceStatus();

    /**
     * Kiểm tra và validate business rules cho bảng giá
     * 
     * @param request yêu cầu tạo/cập nhật bảng giá
     * @param isUpdate có phải là cập nhật không
     * @param currentPriceId ID bảng giá hiện tại (nếu là cập nhật)
     */
    void validatePriceBusinessRules(PriceCreateRequest request, boolean isUpdate, Long currentPriceId);

    /**
     * Lấy giá hiện tại của biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return chi tiết giá hiện tại
     */
    PriceDetailDto getCurrentPriceByVariantId(Long variantId);

    /**
     * Lấy danh sách chi tiết giá theo ID bảng giá
     * 
     * @param priceId ID bảng giá
     * @return danh sách chi tiết giá
     */
    List<PriceDetailDto> getPriceDetailsByPriceId(Long priceId);

    /**
     * Thêm chi tiết giá vào bảng giá
     * 
     * @param priceId ID bảng giá
     * @param priceDetails danh sách chi tiết giá cần thêm
     * @return thông tin bảng giá đã cập nhật
     */
    PriceResponse addPriceDetails(Long priceId, List<PriceCreateRequest.PriceDetailCreateRequest> priceDetails);

    /**
     * Xóa chi tiết giá khỏi bảng giá
     * 
     * @param priceId ID bảng giá
     * @param priceDetailIds danh sách ID chi tiết giá cần xóa
     * @return thông tin bảng giá đã cập nhật
     */
    PriceResponse removePriceDetails(Long priceId, List<Long> priceDetailIds);
}
