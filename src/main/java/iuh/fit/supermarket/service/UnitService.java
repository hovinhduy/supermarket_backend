package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.unit.UnitCreateRequest;
import iuh.fit.supermarket.dto.unit.UnitDto;
import iuh.fit.supermarket.dto.unit.UnitUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho quản lý đơn vị tính
 * Cung cấp các chức năng CRUD và business logic cho đơn vị tính
 */
public interface UnitService {

    /**
     * Tạo đơn vị tính mới
     * 
     * @param request thông tin đơn vị tính cần tạo
     * @return thông tin đơn vị tính đã tạo
     */
    UnitDto createUnit(UnitCreateRequest request);

    /**
     * Cập nhật thông tin đơn vị tính
     * 
     * @param id ID đơn vị tính
     * @param request thông tin cập nhật
     * @return thông tin đơn vị tính đã cập nhật
     */
    UnitDto updateUnit(Long id, UnitUpdateRequest request);

    /**
     * Lấy thông tin đơn vị tính theo ID
     * 
     * @param id ID đơn vị tính
     * @return thông tin đơn vị tính
     */
    UnitDto getUnitById(Long id);

    /**
     * Lấy thông tin đơn vị tính theo tên
     *
     * @param name tên đơn vị tính
     * @return thông tin đơn vị tính
     */
    UnitDto getUnitByCode(String name);

    /**
     * Lấy danh sách tất cả đơn vị tính (có phân trang)
     *
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    Page<UnitDto> getAllUnits(Pageable pageable);

    /**
     * Lấy danh sách đơn vị tính hoạt động
     *
     * @return danh sách đơn vị tính hoạt động
     */
    List<UnitDto> getActiveUnits();

    /**
     * Tìm kiếm đơn vị tính theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    Page<UnitDto> searchUnits(String keyword, Pageable pageable);

    /**
     * Lấy danh sách đơn vị tính theo trạng thái hoạt động
     * 
     * @param isActive trạng thái hoạt động
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    Page<UnitDto> getUnitsByStatus(Boolean isActive, Pageable pageable);

    /**
     * Xóa đơn vị tính (soft delete)
     * 
     * @param id ID đơn vị tính
     */
    void deleteUnit(Long id);

    /**
     * Xóa nhiều đơn vị tính cùng lúc (soft delete)
     * 
     * @param ids danh sách ID đơn vị tính
     */
    void deleteUnits(List<Long> ids);

    /**
     * Kích hoạt/vô hiệu hóa đơn vị tính
     * 
     * @param id ID đơn vị tính
     * @param isActive trạng thái hoạt động mới
     */
    void toggleUnitStatus(Long id, Boolean isActive);

    /**
     * Kiểm tra đơn vị tính có đang được sử dụng không
     * 
     * @param id ID đơn vị tính
     * @return true nếu đang được sử dụng, false nếu không
     */
    boolean isUnitInUse(Long id);

    /**
     * Lấy danh sách đơn vị tính có thể xóa
     * 
     * @return danh sách đơn vị tính có thể xóa
     */
    List<UnitDto> getDeletableUnits();

    /**
     * Đếm số lượng đơn vị tính hoạt động
     *
     * @return số lượng đơn vị tính hoạt động
     */
    Long countActiveUnits();

    /**
     * Tạo dữ liệu mẫu cho đơn vị tính
     * Tạo các đơn vị tính cơ bản như Kilogram, Gram, Lít, Chai, Hộp, v.v.
     */
    void createSampleUnits();
}
