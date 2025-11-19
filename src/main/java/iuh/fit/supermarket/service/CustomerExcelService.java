package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.customer.CreateCustomerRequest;
import iuh.fit.supermarket.dto.customer.CustomerDto;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.Gender;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service xử lý import/export Excel cho khách hàng
 */
@Service
@Slf4j
public class CustomerExcelService {

    // Định nghĩa các cột trong Excel cho Export
    private static final String[] EXPORT_HEADERS = {
            "Mã khách hàng", "Tên khách hàng", "Email", "Số điện thoại",
            "Giới tính", "Ngày sinh", "Địa chỉ", "Loại khách hàng",
            "Ngày tạo", "Ngày cập nhật"
    };

    // Định nghĩa các cột trong Excel cho Import
    private static final String[] IMPORT_HEADERS = {
            "Tên khách hàng", "Email", "Số điện thoại",
            "Giới tính", "Ngày sinh", "Địa chỉ", "Loại khách hàng"
    };

    /**
     * Export danh sách khách hàng ra file Excel
     */
    public byte[] exportCustomersToExcel(List<CustomerDto> customers) throws IOException {
        log.info("Bắt đầu export {} khách hàng ra Excel", customers.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách khách hàng");

            // Tạo style cho header
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Tạo style cho data
            CellStyle dataStyle = createDataStyle(workbook);

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tạo data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;

            for (CustomerDto customer : customers) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // Mã khách hàng
                row.createCell(colNum++)
                        .setCellValue(customer.getCustomerCode() != null ? customer.getCustomerCode() : "");
                // Tên khách hàng
                row.createCell(colNum++).setCellValue(customer.getName() != null ? customer.getName() : "");
                // Email
                row.createCell(colNum++).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
                // Số điện thoại
                row.createCell(colNum++).setCellValue(customer.getPhone() != null ? customer.getPhone() : "");

                // Giới tính
                row.createCell(colNum++).setCellValue(
                        customer.getGender() != null ? (customer.getGender() == Gender.MALE ? "Nam"
                                : customer.getGender() == Gender.FEMALE ? "Nữ" : "Khác") : "");

                // Ngày sinh
                row.createCell(colNum++).setCellValue(
                        customer.getDateOfBirth() != null ? customer.getDateOfBirth().format(dateFormatter) : "");

                // Địa chỉ
                row.createCell(colNum++).setCellValue(customer.getAddress() != null ? customer.getAddress() : "");
                // Loại khách hàng
                row.createCell(colNum++)
                        .setCellValue(customer.getCustomerType() != null ? customer.getCustomerType().name() : "");

                // Ngày tạo
                row.createCell(colNum++).setCellValue(
                        customer.getCreatedAt() != null ? customer.getCreatedAt().format(dateTimeFormatter) : "");
                // Ngày cập nhật
                row.createCell(colNum++).setCellValue(
                        customer.getUpdatedAt() != null ? customer.getUpdatedAt().format(dateTimeFormatter) : "");

                // Apply data style
                for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Export Excel thành công với {} khách hàng", customers.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Import khách hàng từ file Excel
     */
    public List<CreateCustomerRequest> importCustomersFromExcel(MultipartFile file) throws IOException {
        log.info("Bắt đầu import khách hàng từ file Excel: {}", file.getOriginalFilename());

        List<CreateCustomerRequest> customers = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;

                try {
                    // Kiểm tra dòng trống
                    if (isRowEmpty(row)) {
                        continue;
                    }

                    CreateCustomerRequest customer = parseRowToCustomer(row, rowNum);
                    if (customer != null) {
                        customers.add(customer);
                    }
                } catch (Exception e) {
                    log.warn("Lỗi khi parse dòng {}: {}", rowNum, e.getMessage());
                    throw new RuntimeException("Lỗi tại dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        log.info("Import thành công {} khách hàng từ Excel", customers.size());
        return customers;
    }

    /**
     * Parse một dòng Excel thành CreateCustomerRequest
     */
    private CreateCustomerRequest parseRowToCustomer(Row row, int rowNum) {
        try {
            // Index shifted because Customer Code is removed
            // 0: Name, 1: Email, 2: Phone, 3: Gender, 4: DOB, 5: Address, 6: Customer Type

            String name = getCellValueAsString(row.getCell(0));
            String email = getCellValueAsString(row.getCell(1));
            String phone = getCellValueAsString(row.getCell(2));
            String genderStr = getCellValueAsString(row.getCell(3));

            LocalDate dateOfBirth = null;
            Cell dobCell = row.getCell(4);
            if (dobCell != null) {
                if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                    dateOfBirth = dobCell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                } else {
                    String dobStr = getCellValueAsString(dobCell);
                    if (dobStr != null && !dobStr.trim().isEmpty()) {
                        try {
                            dateOfBirth = LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } catch (DateTimeParseException e) {
                            // Try other format if needed or ignore
                        }
                    }
                }
            }

            String address = getCellValueAsString(row.getCell(5));
            String customerTypeStr = getCellValueAsString(row.getCell(6));

            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Tên khách hàng không được để trống");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống");
            }

            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setCustomerCode(null); // Auto-generated
            request.setName(name.trim());
            request.setEmail(email.trim());
            request.setPhone(phone != null ? phone.trim() : null);

            if (genderStr != null) {
                String g = genderStr.trim().toLowerCase();
                if (g.equals("nam") || g.equals("male") || g.equals("trai")) {
                    request.setGender(Gender.MALE);
                } else if (g.equals("nữ") || g.equals("nu") || g.equals("female") || g.equals("gái")) {
                    request.setGender(Gender.FEMALE);
                } else {
                    request.setGender(null);
                }
            }

            request.setDateOfBirth(dateOfBirth);
            request.setAddress(address != null ? address.trim() : null);

            if (customerTypeStr != null) {
                try {
                    request.setCustomerType(CustomerType.valueOf(customerTypeStr.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    request.setCustomerType(CustomerType.REGULAR);
                }
            } else {
                request.setCustomerType(CustomerType.REGULAR);
            }

            return request;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Tạo file template Excel để import khách hàng
     */
    public byte[] createImportTemplate() throws IOException {
        log.info("Tạo template Excel import khách hàng");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Import Khách Hàng");

            // Tạo style cho header
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Tạo style cho example data
            CellStyle exampleStyle = createDataStyle(workbook);

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tạo dòng ví dụ
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {
                    "Nguyễn Văn A", // Tên khách hàng
                    "nguyenvana@example.com", // Email
                    "0901234567", // Số điện thoại
                    "Nam", // Giới tính
                    "01/01/1990", // Ngày sinh
                    "123 Đường ABC, Quận 1, TP.HCM", // Địa chỉ
                    "REGULAR" // Loại khách hàng
            };

            for (int i = 0; i < exampleData.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(exampleData[i]);
                cell.setCellStyle(exampleStyle);
            }

            // Tạo sheet hướng dẫn
            Sheet instructionSheet = workbook.createSheet("Hướng Dẫn");
            createInstructionSheet(instructionSheet, workbook);

            // Auto-size columns
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Tạo template Excel thành công");
            return outputStream.toByteArray();
        }
    }

    private void createInstructionSheet(Sheet sheet, Workbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        CellStyle normalStyle = workbook.createCellStyle();
        Font normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 11);
        normalStyle.setFont(normalFont);

        int rowNum = 0;

        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HƯỚNG DẪN SỬ DỤNG TEMPLATE IMPORT KHÁCH HÀNG");
        titleCell.setCellStyle(titleStyle);

        rowNum++; // Dòng trống

        // Hướng dẫn các cột
        String[] instructions = {
                "1. Tên khách hàng: Bắt buộc",
                "2. Email: Bắt buộc, phải đúng định dạng email",
                "3. Số điện thoại: Tùy chọn, 10 số",
                "4. Giới tính: Nam/Nữ/Khác (hoặc Male/Female/Other)",
                "5. Ngày sinh: Định dạng dd/MM/yyyy (VD: 31/12/1990)",
                "6. Địa chỉ: Tùy chọn",
                "7. Loại khách hàng: REGULAR hoặc VIP (mặc định REGULAR)",
                "",
                "LƯU Ý:",
                "- Dòng đầu tiên là header, không được xóa",
                "- Email và Số điện thoại phải là duy nhất trong hệ thống",
                "- Mã khách hàng sẽ được hệ thống tự động sinh"
        };

        for (String instruction : instructions) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            cell.setCellStyle(normalStyle);
        }

        sheet.autoSizeColumn(0);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}
