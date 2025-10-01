package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.stocktake.*;
import iuh.fit.supermarket.enums.StocktakeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho quản lý kiểm kê kho
 */
public interface StocktakeService {

    /**
     * Tạo phiếu kiểm kê mới
     * 
     * @param request thông tin phiếu kiểm kê cần tạo
     * @return thông tin phiếu kiểm kê đã tạo
     */
    StocktakeDto createStocktake(StocktakeCreateRequest request);

    /**
     * Lấy thông tin phiếu kiểm kê theo ID
     * 
     * @param stocktakeId ID phiếu kiểm kê
     * @return thông tin phiếu kiểm kê
     */
    StocktakeDto getStocktakeById(Integer stocktakeId);

    /**
     * Lấy thông tin phiếu kiểm kê theo mã phiếu
     * 
     * @param stocktakeCode mã phiếu kiểm kê
     * @return thông tin phiếu kiểm kê
     */
    StocktakeDto getStocktakeByCode(String stocktakeCode);

    /**
     * Cập nhật thông tin phiếu kiểm kê
     * 
     * @param stocktakeId ID phiếu kiểm kê
     * @param request     thông tin cập nhật
     * @return thông tin phiếu kiểm kê đã cập nhật
     */
    StocktakeDto updateStocktake(Integer stocktakeId, StocktakeUpdateRequest request);

    /**
     * Hoàn thành phiếu kiểm kê (chuyển từ PENDING sang COMPLETED)
     * Cập nhật tồn kho và tạo WarehouseTransaction
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return thông tin phiếu kiểm kê đã hoàn thành
     */
    StocktakeDto completeStocktake(Integer stocktakeId);

    /**
     * Xóa phiếu kiểm kê (chỉ cho phép khi trạng thái PENDING)
     * 
     * @param stocktakeId ID phiếu kiểm kê
     */
    void deleteStocktake(Integer stocktakeId);

    /**
     * Lấy danh sách phiếu kiểm kê với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return danh sách phiếu kiểm kê
     */
    Page<StocktakeDto> getAllStocktakes(Pageable pageable);

    /**
     * Lấy danh sách phiếu kiểm kê theo trạng thái
     * 
     * @param status   trạng thái kiểm kê
     * @param pageable thông tin phân trang
     * @return danh sách phiếu kiểm kê
     */
    Page<StocktakeDto> getStocktakesByStatus(StocktakeStatus status, Pageable pageable);

    /**
     * Tìm kiếm phiếu kiểm kê theo từ khóa
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return danh sách phiếu kiểm kê
     */
    Page<StocktakeDto> searchStocktakes(String keyword, Pageable pageable);

    /**
     * Lấy danh sách phiếu kiểm kê trong khoảng thời gian
     * 
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @param pageable  thông tin phân trang
     * @return danh sách phiếu kiểm kê
     */
    Page<StocktakeDto> getStocktakesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Lấy danh sách chi tiết kiểm kê theo phiếu kiểm kê
     * 
     * @param stocktakeId ID phiếu kiểm kê
     * @return danh sách chi tiết kiểm kê
     */
    List<StocktakeDetailDto> getStocktakeDetails(Integer stocktakeId);

    /**
     * Thêm chi tiết kiểm kê vào phiếu kiểm kê
     * 
     * @param request thông tin chi tiết kiểm kê
     * @return thông tin chi tiết kiểm kê đã tạo
     */
    StocktakeDetailDto addStocktakeDetail(StocktakeDetailCreateRequest request);

    /**
     * Cập nhật chi tiết kiểm kê
     * 
     * @param detailId        ID chi tiết kiểm kê
     * @param quantityCounted số lượng thực tế đếm được
     * @param reason          ghi chú lý do chênh lệch
     * @return thông tin chi tiết kiểm kê đã cập nhật
     */
    StocktakeDetailDto updateStocktakeDetail(Integer detailId, Integer quantityCounted, String reason);

    /**
     * Xóa chi tiết kiểm kê
     * 
     * @param detailId ID chi tiết kiểm kê
     */
    void deleteStocktakeDetail(Integer detailId);

    /**
     * Lấy danh sách chi tiết có chênh lệch
     * 
     * @param stocktakeId ID phiếu kiểm kê
     * @return danh sách chi tiết có chênh lệch
     */
    List<StocktakeDetailDto> getStocktakeDifferences(Integer stocktakeId);

    /**
     * Tạo phiếu kiểm kê từ danh sách tồn kho hiện tại
     *
     * @param notes          ghi chú
     * @param productUnitIds danh sách ID đơn vị sản phẩm cần kiểm kê (null = tất
     *                       cả)
     * @param status         trạng thái phiếu kiểm kê (mặc định PENDING)
     * @return thông tin phiếu kiểm kê đã tạo
     */
    StocktakeDto createStocktakeFromCurrentStock(String notes, List<Long> productUnitIds, StocktakeStatus status);

    /**
     * Kiểm tra xem có phiếu kiểm kê nào đang PENDING không
     * 
     * @return true nếu có phiếu kiểm kê đang PENDING
     */
    boolean hasPendingStocktake();

    /**
     * Lấy thống kê tổng quan về kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return thống kê tổng quan
     */
    StocktakeDto.StocktakeSummary getStocktakeSummary(Integer stocktakeId);

    /**
     * Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê
     * Đảm bảo quantityExpected phản ánh đúng số lượng tồn kho hiện tại
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return thông tin phiếu kiểm kê đã cập nhật
     * @throws StocktakeNotFoundException nếu không tìm thấy phiếu kiểm kê
     * @throws StocktakeException         nếu phiếu kiểm kê đã hoàn thành
     */
    StocktakeDto refreshExpectedQuantities(Integer stocktakeId);
}
