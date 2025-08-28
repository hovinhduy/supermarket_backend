package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.employee.EmployeeDto;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.EmployeeRole;
import iuh.fit.supermarket.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý các API liên quan đến nhân viên
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "API quản lý nhân viên")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Lấy danh sách tất cả nhân viên (chỉ ADMIN và MANAGER)
     */
    @Operation(summary = "Lấy danh sách tất cả nhân viên", description = "Lấy danh sách tất cả nhân viên trong hệ thống. Chỉ ADMIN và MANAGER mới có quyền truy cập.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees() {
        log.info("Nhận yêu cầu lấy danh sách tất cả nhân viên");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(EmployeeDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách nhân viên thành công", employeeDtos));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhân viên", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi lấy danh sách nhân viên"));
        }
    }

    /**
     * Lấy thông tin nhân viên theo ID (chỉ ADMIN và MANAGER)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable Integer id) {
        log.info("Nhận yêu cầu lấy nhân viên với ID: {}", id);

        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);

            if (employee.isPresent()) {
                EmployeeDto employeeDto = EmployeeDto.fromEntity(employee.get());
                return ResponseEntity.ok(
                        ApiResponse.success("Lấy thông tin nhân viên thành công", employeeDto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với ID: " + id));
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin nhân viên với ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi lấy thông tin nhân viên"));
        }
    }

    /**
     * Tạo nhân viên mới (chỉ ADMIN)
     */
    @Operation(summary = "Tạo nhân viên mới", description = "Tạo một nhân viên mới trong hệ thống. Chỉ ADMIN mới có quyền thực hiện.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo nhân viên thành công", content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Parameter(description = "Thông tin nhân viên mới", required = true) @RequestBody Employee employee) {
        log.info("Nhận yêu cầu tạo nhân viên mới với email: {}", employee.getEmail());

        try {
            Employee createdEmployee = employeeService.createEmployee(employee);
            EmployeeDto employeeDto = EmployeeDto.fromEntity(createdEmployee);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo nhân viên thành công", employeeDto));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi tạo nhân viên: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo nhân viên", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi tạo nhân viên"));
        }
    }

    /**
     * Cập nhật thông tin nhân viên (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @PathVariable Integer id,
            @RequestBody Employee employee) {
        log.info("Nhận yêu cầu cập nhật nhân viên với ID: {}", id);

        try {
            Employee updatedEmployee = employeeService.updateEmployee(id, employee);
            EmployeeDto employeeDto = EmployeeDto.fromEntity(updatedEmployee);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật nhân viên thành công", employeeDto));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi cập nhật nhân viên: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật nhân viên với ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi cập nhật nhân viên"));
        }
    }

    /**
     * Xóa nhân viên (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Integer id) {
        log.info("Nhận yêu cầu xóa nhân viên với ID: {}", id);

        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Xóa nhân viên thành công", "Nhân viên đã được xóa"));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi khi xóa nhân viên: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi xóa nhân viên với ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi xóa nhân viên"));
        }
    }

    /**
     * Lấy nhân viên theo role (chỉ ADMIN và MANAGER)
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getEmployeesByRole(@PathVariable EmployeeRole role) {
        log.info("Nhận yêu cầu lấy nhân viên theo role: {}", role);

        try {
            List<Employee> employees = employeeService.getEmployeesByRole(role);
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(EmployeeDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách nhân viên theo role thành công", employeeDtos));
        } catch (Exception e) {
            log.error("Lỗi khi lấy nhân viên theo role: {}", role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi lấy danh sách nhân viên"));
        }
    }

    /**
     * Tìm kiếm nhân viên theo tên (tất cả role đã đăng nhập)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> searchEmployees(@RequestParam String name) {
        log.info("Nhận yêu cầu tìm kiếm nhân viên với tên: {}", name);

        try {
            List<Employee> employees = employeeService.searchEmployeesByName(name);
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(EmployeeDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(
                    ApiResponse.success("Tìm kiếm nhân viên thành công", employeeDtos));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm nhân viên với tên: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi tìm kiếm nhân viên"));
        }
    }

    /**
     * Đổi mật khẩu (chỉ ADMIN hoặc chính nhân viên đó)
     */
    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @employeeService.getEmployeeById(#id).orElse(new iuh.fit.supermarket.entity.Employee()).email")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Integer id,
            @RequestParam String newPassword) {
        log.info("Nhận yêu cầu đổi mật khẩu cho nhân viên với ID: {}", id);

        try {
            employeeService.changePassword(id, newPassword);
            return ResponseEntity.ok(
                    ApiResponse.success("Đổi mật khẩu thành công", "Mật khẩu đã được cập nhật"));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi khi đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi đổi mật khẩu cho nhân viên với ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi đổi mật khẩu"));
        }
    }
}
