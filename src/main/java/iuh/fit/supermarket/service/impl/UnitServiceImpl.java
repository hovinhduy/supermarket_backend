package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.unit.UnitCreateRequest;
import iuh.fit.supermarket.dto.unit.UnitDto;
import iuh.fit.supermarket.dto.unit.UnitUpdateRequest;
import iuh.fit.supermarket.entity.Unit;
import iuh.fit.supermarket.repository.UnitRepository;
import iuh.fit.supermarket.service.UnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của UnitService
 * Xử lý logic nghiệp vụ cho quản lý đơn vị tính
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    /**
     * Tạo đơn vị tính mới
     */
    @Override
    public UnitDto createUnit(UnitCreateRequest request) {
        log.info("Tạo đơn vị tính mới với tên: {}", request.getName());

        // Kiểm tra trùng lặp tên
        if (unitRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên đơn vị tính đã tồn tại: " + request.getName());
        }

        // Tạo entity
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        unit.setIsDeleted(false);

        // Lưu vào database
        unit = unitRepository.save(unit);
        log.info("Đã tạo đơn vị tính với ID: {}", unit.getId());

        return mapToDto(unit);
    }

    /**
     * Cập nhật thông tin đơn vị tính
     */
    @Override
    public UnitDto updateUnit(Long id, UnitUpdateRequest request) {
        log.info("Cập nhật đơn vị tính ID: {}", id);

        // Tìm đơn vị tính
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị tính với ID: " + id));

        // Kiểm tra có dữ liệu cập nhật không
        if (!request.hasUpdates()) {
            throw new RuntimeException("Không có dữ liệu để cập nhật");
        }

        // Cập nhật các trường
        if (request.getName() != null) {
            // Kiểm tra trùng lặp tên (ngoại trừ chính nó)
            if (!unit.getName().equals(request.getName()) && unitRepository.existsByName(request.getName())) {
                throw new RuntimeException("Tên đơn vị tính đã tồn tại: " + request.getName());
            }
            unit.setName(request.getName());
        }

        if (request.getIsActive() != null) {
            unit.setIsActive(request.getIsActive());
        }

        // Lưu cập nhật
        unit = unitRepository.save(unit);
        log.info("Đã cập nhật đơn vị tính ID: {}", id);

        return mapToDto(unit);
    }

    /**
     * Lấy thông tin đơn vị tính theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public UnitDto getUnitById(Long id) {
        log.info("Lấy thông tin đơn vị tính ID: {}", id);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị tính với ID: " + id));

        return mapToDto(unit);
    }

    /**
     * Lấy thông tin đơn vị tính theo tên
     */
    @Override
    @Transactional(readOnly = true)
    public UnitDto getUnitByCode(String name) {
        log.info("Lấy thông tin đơn vị tính theo tên: {}", name);

        Unit unit = unitRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị tính với tên: " + name));

        return mapToDto(unit);
    }

    /**
     * Lấy danh sách tất cả đơn vị tính (có phân trang)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UnitDto> getAllUnits(Pageable pageable) {
        log.info("Lấy danh sách tất cả đơn vị tính với phân trang");

        Page<Unit> units = unitRepository.findAllNotDeleted(pageable);
        return units.map(this::mapToDto);
    }

    /**
     * Lấy danh sách đơn vị tính hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<UnitDto> getActiveUnits() {
        log.info("Lấy danh sách đơn vị tính hoạt động");

        List<Unit> units = unitRepository.findActiveUnits();
        return units.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }



    /**
     * Tìm kiếm đơn vị tính theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UnitDto> searchUnits(String keyword, Pageable pageable) {
        log.info("Tìm kiếm đơn vị tính với từ khóa: {}", keyword);

        Page<Unit> units = unitRepository.findByKeyword(keyword, pageable);
        return units.map(this::mapToDto);
    }

    /**
     * Lấy danh sách đơn vị tính theo trạng thái hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UnitDto> getUnitsByStatus(Boolean isActive, Pageable pageable) {
        log.info("Lấy danh sách đơn vị tính theo trạng thái: {}", isActive);

        Page<Unit> units = unitRepository.findByIsActive(isActive, pageable);
        return units.map(this::mapToDto);
    }

    /**
     * Xóa đơn vị tính (soft delete)
     */
    @Override
    public void deleteUnit(Long id) {
        log.info("Xóa đơn vị tính ID: {}", id);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị tính với ID: " + id));

        // Kiểm tra có đang được sử dụng không
        if (unitRepository.isUnitInUse(id)) {
            throw new RuntimeException("Không thể xóa đơn vị tính đang được sử dụng");
        }

        // Soft delete
        unit.setIsDeleted(true);
        unit.setIsActive(false);
        unitRepository.save(unit);

        log.info("Đã xóa đơn vị tính ID: {}", id);
    }

    /**
     * Xóa nhiều đơn vị tính cùng lúc (soft delete)
     */
    @Override
    public void deleteUnits(List<Long> ids) {
        log.info("Xóa {} đơn vị tính", ids.size());

        for (Long id : ids) {
            try {
                deleteUnit(id);
            } catch (Exception e) {
                log.warn("Không thể xóa đơn vị tính ID {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa đơn vị tính
     */
    @Override
    public void toggleUnitStatus(Long id, Boolean isActive) {
        log.info("Thay đổi trạng thái đơn vị tính ID {} thành: {}", id, isActive);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị tính với ID: " + id));

        unit.setIsActive(isActive);
        unitRepository.save(unit);

        log.info("Đã thay đổi trạng thái đơn vị tính ID: {}", id);
    }

    /**
     * Kiểm tra đơn vị tính có đang được sử dụng không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUnitInUse(Long id) {
        return unitRepository.isUnitInUse(id);
    }

    /**
     * Lấy danh sách đơn vị tính có thể xóa
     */
    @Override
    @Transactional(readOnly = true)
    public List<UnitDto> getDeletableUnits() {
        log.info("Lấy danh sách đơn vị tính có thể xóa");

        List<Unit> units = unitRepository.findDeletableUnits();
        return units.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lượng đơn vị tính hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public Long countActiveUnits() {
        return unitRepository.countActiveUnits();
    }



    /**
     * Tạo dữ liệu mẫu cho đơn vị tính
     */
    @Override
    public void createSampleUnits() {
        log.info("Tạo dữ liệu mẫu cho đơn vị tính");

        // Danh sách đơn vị tính mẫu
        String[] sampleUnitNames = {
            "Kilogram", "Gram", "Tấn",
            "Lít", "Mililit",
            "Cái", "Hộp", "Chai", "Lon", "Gói", "Thùng",
            "Mét", "Centimet", "Mét vuông",
            "Giờ", "Ngày"
        };

        // Tạo từng đơn vị tính
        for (String unitName : sampleUnitNames) {
            try {
                // Kiểm tra đã tồn tại chưa
                if (!unitRepository.existsByName(unitName)) {
                    UnitCreateRequest request = new UnitCreateRequest(unitName);
                    createUnit(request);
                    log.info("Đã tạo đơn vị tính mẫu: {}", unitName);
                } else {
                    log.info("Đơn vị tính đã tồn tại: {}", unitName);
                }
            } catch (Exception e) {
                log.error("Lỗi khi tạo đơn vị tính mẫu {}: {}", unitName, e.getMessage());
            }
        }

        log.info("Hoàn thành tạo dữ liệu mẫu cho đơn vị tính");
    }

    /**
     * Chuyển đổi Entity sang DTO
     */
    private UnitDto mapToDto(Unit unit) {
        UnitDto dto = new UnitDto();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setIsActive(unit.getIsActive());
        dto.setIsDeleted(unit.getIsDeleted());
        dto.setCreatedAt(unit.getCreatedAt());
        dto.setUpdatedAt(unit.getUpdatedAt());

        // Thêm thông tin bổ sung
        dto.setUsageCount(0L); // TODO: Implement usage count logic
        dto.setCanDelete(!unitRepository.isUnitInUse(unit.getId()));

        return dto;
    }
}
