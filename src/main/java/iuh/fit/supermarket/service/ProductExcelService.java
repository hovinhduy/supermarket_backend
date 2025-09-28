package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.ProductCreateRequest;
import iuh.fit.supermarket.dto.product.ProductResponse;
import iuh.fit.supermarket.dto.product.ProductUnitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service xử lý import/export Excel cho sản phẩm
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductExcelService {

    private final ProductService productService;

    // Định nghĩa các cột trong Excel
    private static final String[] EXPORT_HEADERS = {
        "ID", "Tên sản phẩm", "Mô tả", "Thương hiệu", "Danh mục", 
        "Trạng thái", "Tích điểm", "Ngày tạo", "Ngày cập nhật"
    };

    private static final String[] IMPORT_HEADERS = {
        "Tên sản phẩm", "Mô tả", "ID Thương hiệu", "ID Danh mục", 
        "Trạng thái", "Tích điểm", "Tên đơn vị", "Tỷ lệ quy đổi", "Đơn vị cơ bản", "Mã vạch"
    };

    /**
     * Export danh sách sản phẩm ra file Excel
     */
    public byte[] exportProductsToExcel(List<ProductResponse> products) throws IOException {
        log.info("Bắt đầu export {} sản phẩm ra Excel", products.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách sản phẩm");

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            
            for (ProductResponse product : products) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(product.getId() != null ? product.getId() : 0);
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "");
                row.createCell(2).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                row.createCell(3).setCellValue(product.getBrand() != null ? product.getBrand().getName() : "");
                row.createCell(4).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(5).setCellValue(product.getIsActive() != null && product.getIsActive() ? "Hoạt động" : "Không hoạt động");
                row.createCell(6).setCellValue(product.getIsRewardPoint() != null && product.getIsRewardPoint() ? "Có" : "Không");
                row.createCell(7).setCellValue(product.getCreatedDate() != null ? product.getCreatedDate().format(formatter) : "");
                row.createCell(8).setCellValue(product.getUpdatedAt() != null ? product.getUpdatedAt().format(formatter) : "");

                // Apply data style
                for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("Export Excel thành công với {} sản phẩm", products.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Import sản phẩm từ file Excel
     */
    public List<ProductCreateRequest> importProductsFromExcel(MultipartFile file) throws IOException {
        log.info("Bắt đầu import sản phẩm từ file Excel: {}", file.getOriginalFilename());

        List<ProductCreateRequest> products = new ArrayList<>();

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
                
                try {
                    ProductCreateRequest product = parseRowToProduct(row, rowNum);
                    if (product != null) {
                        products.add(product);
                    }
                } catch (Exception e) {
                    log.warn("Lỗi khi parse dòng {}: {}", rowNum, e.getMessage());
                    throw new RuntimeException("Lỗi tại dòng " + rowNum + ": " + e.getMessage());
                }
                
                rowNum++;
            }
        }

        log.info("Import thành công {} sản phẩm từ Excel", products.size());
        return products;
    }

    /**
     * Parse một dòng Excel thành ProductCreateRequest
     */
    private ProductCreateRequest parseRowToProduct(Row row, int rowNum) {
        // Kiểm tra dòng trống
        if (isRowEmpty(row)) {
            return null;
        }

        try {
            String name = getCellValueAsString(row.getCell(0));
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Tên sản phẩm không được để trống");
            }

            String description = getCellValueAsString(row.getCell(1));
            Integer brandId = getCellValueAsInteger(row.getCell(2));
            Integer categoryId = getCellValueAsInteger(row.getCell(3));
            
            if (categoryId == null) {
                throw new RuntimeException("ID danh mục không được để trống");
            }

            Boolean isActive = getCellValueAsBoolean(row.getCell(4));
            Boolean isRewardPoint = getCellValueAsBoolean(row.getCell(5));

            // Parse unit information
            String unitName = getCellValueAsString(row.getCell(6));
            Integer conversionValue = getCellValueAsInteger(row.getCell(7));
            Boolean isBaseUnit = getCellValueAsBoolean(row.getCell(8));
            String barcode = getCellValueAsString(row.getCell(9));

            if (unitName == null || unitName.trim().isEmpty()) {
                throw new RuntimeException("Tên đơn vị không được để trống");
            }
            if (conversionValue == null || conversionValue <= 0) {
                conversionValue = 1;
            }
            if (isBaseUnit == null) {
                isBaseUnit = true;
            }

            // Tạo ProductUnitRequest
            ProductUnitRequest unitRequest = new ProductUnitRequest(
                unitName.trim(),
                conversionValue,
                isBaseUnit,
                null, // code sẽ được tự động tạo
                barcode
            );

            // Tạo ProductCreateRequest
            ProductCreateRequest product = new ProductCreateRequest();
            product.setName(name.trim());
            product.setDescription(description);
            product.setBrandId(brandId);
            product.setCategoryId(categoryId);
            product.setIsActive(isActive != null ? isActive : true);
            product.setIsRewardPoint(isRewardPoint != null ? isRewardPoint : false);
            product.setUnits(List.of(unitRequest));

            return product;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra dòng có trống không
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        
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

    /**
     * Lấy giá trị cell dưới dạng String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
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

    /**
     * Lấy giá trị cell dưới dạng Integer
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : Integer.parseInt(value);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Lấy giá trị cell dưới dạng Boolean
     */
    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().toLowerCase();
                return "true".equals(value) || "có".equals(value) || "hoạt động".equals(value) || "1".equals(value);
            case NUMERIC:
                return cell.getNumericCellValue() > 0;
            default:
                return null;
        }
    }

    /**
     * Tạo style cho header
     */
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

    /**
     * Tạo style cho data
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * Tạo file template Excel để import sản phẩm
     */
    public byte[] createImportTemplate() throws IOException {
        log.info("Tạo template Excel import sản phẩm");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Import Sản Phẩm");

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
                "Coca Cola 330ml",           // Tên sản phẩm
                "Nước ngọt có gas",          // Mô tả
                "1",                         // ID Thương hiệu
                "1",                         // ID Danh mục
                "true",                      // Trạng thái (true/false hoặc Hoạt động/Không hoạt động)
                "false",                     // Tích điểm (true/false hoặc Có/Không)
                "Chai",                      // Tên đơn vị
                "1",                         // Tỷ lệ quy đổi
                "true",                      // Đơn vị cơ bản (true/false)
                "1234567890123"              // Mã vạch
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

    /**
     * Tạo sheet hướng dẫn sử dụng template
     */
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
        titleCell.setCellValue("HƯỚNG DẪN SỬ DỤNG TEMPLATE IMPORT SẢN PHẨM");
        titleCell.setCellStyle(titleStyle);

        rowNum++; // Dòng trống

        // Hướng dẫn các cột
        String[] instructions = {
            "1. Tên sản phẩm: Bắt buộc, không được để trống",
            "2. Mô tả: Tùy chọn, mô tả chi tiết về sản phẩm",
            "3. ID Thương hiệu: Tùy chọn, ID của thương hiệu (số nguyên)",
            "4. ID Danh mục: Bắt buộc, ID của danh mục sản phẩm (số nguyên)",
            "5. Trạng thái: true/false hoặc Hoạt động/Không hoạt động (mặc định: true)",
            "6. Tích điểm: true/false hoặc Có/Không (mặc định: false)",
            "7. Tên đơn vị: Bắt buộc, tên đơn vị tính (VD: Chai, Hộp, Kg)",
            "8. Tỷ lệ quy đổi: Bắt buộc, tỷ lệ quy đổi so với đơn vị cơ bản (số nguyên > 0)",
            "9. Đơn vị cơ bản: true/false, đánh dấu đây có phải đơn vị cơ bản không",
            "10. Mã vạch: Tùy chọn, mã vạch của sản phẩm",
            "",
            "LƯU Ý:",
            "- Dòng đầu tiên là header, không được xóa hoặc sửa đổi",
            "- Dữ liệu bắt đầu từ dòng thứ 2",
            "- Các trường bắt buộc không được để trống",
            "- File phải có định dạng .xlsx hoặc .xls",
            "- Nếu có lỗi, hệ thống sẽ báo cáo chi tiết dòng bị lỗi"
        };

        for (String instruction : instructions) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            cell.setCellStyle(normalStyle);
        }

        // Auto-size column
        sheet.autoSizeColumn(0);
    }
}
