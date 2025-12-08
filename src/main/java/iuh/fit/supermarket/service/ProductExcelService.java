package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.ProductCreateRequest;
import iuh.fit.supermarket.dto.product.ProductResponse;
import iuh.fit.supermarket.dto.product.ProductUnitRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý import/export Excel cho sản phẩm
 */
@Service
@Slf4j
public class ProductExcelService {

    private final iuh.fit.supermarket.repository.CategoryRepository categoryRepository;
    private final iuh.fit.supermarket.repository.BrandRepository brandRepository;

    public ProductExcelService(
            iuh.fit.supermarket.repository.CategoryRepository categoryRepository,
            iuh.fit.supermarket.repository.BrandRepository brandRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    // Định nghĩa các cột trong Excel cho Export
    private static final String[] EXPORT_HEADERS = {
            "Mã sản phẩm", "Tên sản phẩm", "Mô tả", "Tên Thương hiệu",
            "Danh mục", "Trạng thái", "Tích điểm",
            "Tên đơn vị", "Tỷ lệ quy đổi", "Đơn vị cơ bản", "Mã vạch",
            "Ngày tạo", "Ngày cập nhật"
    };

    // Định nghĩa các cột trong Excel cho Import
    private static final String[] IMPORT_HEADERS = {
            "Mã sản phẩm", "Tên sản phẩm", "Mô tả", "Tên Thương hiệu", "Tên Danh mục",
            "Trạng thái", "Tích điểm", "Tên đơn vị", "Tỷ lệ quy đổi", "Đơn vị cơ bản", "Mã vạch"
    };

    /**
     * Export danh sách sản phẩm ra file Excel
     */
    @Transactional(readOnly = true)
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

            // Tạo data rows - mỗi ProductUnit là một dòng
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            int totalUnits = 0;

            for (ProductResponse product : products) {
                // Nếu sản phẩm có ProductUnits, export từng unit
                if (product.getProductUnits() != null && !product.getProductUnits().isEmpty()) {
                    for (ProductResponse.ProductUnitInfo unit : product.getProductUnits()) {
                        Row row = sheet.createRow(rowNum++);
                        int colNum = 0;

                        // Thông tin sản phẩm
                        row.createCell(colNum++).setCellValue(product.getCode() != null ? product.getCode() : "");
                        row.createCell(colNum++).setCellValue(product.getName() != null ? product.getName() : "");
                        row.createCell(colNum++)
                                .setCellValue(product.getDescription() != null ? product.getDescription() : "");
                        row.createCell(colNum++)
                                .setCellValue(product.getBrand() != null ? product.getBrand().getName() : "");
                        row.createCell(colNum++)
                                .setCellValue(product.getCategory() != null ? getCategoryFullPath(product.getCategory())
                                        : "");
                        row.createCell(colNum++)
                                .setCellValue(product.getIsActive() != null && product.getIsActive() ? "Hoạt động"
                                        : "Không hoạt động");
                        row.createCell(colNum++).setCellValue(
                                product.getIsRewardPoint() != null && product.getIsRewardPoint() ? "Có" : "Không");

                        // Thông tin đơn vị
                        row.createCell(colNum++).setCellValue(unit.getUnitName() != null ? unit.getUnitName() : "");
                        row.createCell(colNum++)
                                .setCellValue(unit.getConversionValue() != null ? unit.getConversionValue() : 1);
                        row.createCell(colNum++)
                                .setCellValue(unit.getIsBaseUnit() != null && unit.getIsBaseUnit() ? "Có" : "Không");
                        row.createCell(colNum++).setCellValue(unit.getBarcode() != null ? unit.getBarcode() : "");

                        // Ngày tạo và cập nhật
                        row.createCell(colNum++).setCellValue(
                                product.getCreatedDate() != null ? product.getCreatedDate().format(formatter) : "");
                        row.createCell(colNum++).setCellValue(
                                product.getUpdatedAt() != null ? product.getUpdatedAt().format(formatter) : "");

                        // Apply data style
                        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                            Cell cell = row.getCell(i);
                            if (cell != null) {
                                cell.setCellStyle(dataStyle);
                            }
                        }
                        totalUnits++;
                    }
                } else {
                    // Nếu không có unit, vẫn export thông tin sản phẩm
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;

                    row.createCell(colNum++).setCellValue(product.getCode() != null ? product.getCode() : "");
                    row.createCell(colNum++).setCellValue(product.getName() != null ? product.getName() : "");
                    row.createCell(colNum++)
                            .setCellValue(product.getDescription() != null ? product.getDescription() : "");
                    row.createCell(colNum++)
                            .setCellValue(product.getBrand() != null ? product.getBrand().getName() : "");
                    row.createCell(colNum++)
                            .setCellValue(
                                    product.getCategory() != null ? getCategoryFullPath(product.getCategory()) : "");
                    row.createCell(colNum++).setCellValue(
                            product.getIsActive() != null && product.getIsActive() ? "Hoạt động" : "Không hoạt động");
                    row.createCell(colNum++).setCellValue(
                            product.getIsRewardPoint() != null && product.getIsRewardPoint() ? "Có" : "Không");

                    // Các cột unit để trống
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");

                    row.createCell(colNum++).setCellValue(
                            product.getCreatedDate() != null ? product.getCreatedDate().format(formatter) : "");
                    row.createCell(colNum++).setCellValue(
                            product.getUpdatedAt() != null ? product.getUpdatedAt().format(formatter) : "");

                    // Apply data style
                    for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null) {
                            cell.setCellStyle(dataStyle);
                        }
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

            log.info("Export Excel thành công với {} sản phẩm, {} đơn vị", products.size(), totalUnits);
            return outputStream.toByteArray();
        }
    }

    /**
     * Import sản phẩm từ file Excel
     * Hỗ trợ nhiều đơn vị cho cùng một sản phẩm (các dòng có cùng mã sản phẩm)
     */
    public List<ProductCreateRequest> importProductsFromExcel(MultipartFile file) throws IOException {
        log.info("Bắt đầu import sản phẩm từ file Excel: {}", file.getOriginalFilename());

        java.util.Map<String, ProductCreateRequest> productMap = new java.util.LinkedHashMap<>();

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

                    // Parse thông tin sản phẩm và đơn vị
                    String productCode = getCellValueAsString(row.getCell(0));
                    String productName = getCellValueAsString(row.getCell(1));

                    if (productName == null || productName.trim().isEmpty()) {
                        throw new RuntimeException("Tên sản phẩm không được để trống");
                    }

                    // Sử dụng mã sản phẩm hoặc tên sản phẩm làm key
                    String key = (productCode != null && !productCode.trim().isEmpty())
                            ? productCode.trim()
                            : productName.trim();

                    // Nếu sản phẩm đã tồn tại trong map, thêm unit mới
                    if (productMap.containsKey(key)) {
                        ProductCreateRequest existingProduct = productMap.get(key);
                        ProductUnitRequest newUnit = parseUnitFromRow(row, rowNum);

                        if (newUnit != null) {
                            // Thêm unit mới vào danh sách units
                            List<ProductUnitRequest> units = new ArrayList<>(existingProduct.getUnits());
                            units.add(newUnit);
                            existingProduct.setUnits(units);
                            log.debug("Thêm đơn vị '{}' cho sản phẩm '{}'", newUnit.unitName(), key);
                        }
                    } else {
                        // Tạo sản phẩm mới
                        ProductCreateRequest product = parseRowToProduct(row, rowNum);
                        if (product != null) {
                            productMap.put(key, product);
                            log.debug("Tạo sản phẩm mới: {}", key);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Lỗi khi parse dòng {}: {}", rowNum, e.getMessage());
                    throw new RuntimeException("Lỗi tại dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        List<ProductCreateRequest> products = new ArrayList<>(productMap.values());
        log.info("Import thành công {} sản phẩm từ Excel", products.size());
        return products;
    }

    /**
     * Parse một dòng Excel thành ProductCreateRequest
     * Format: Mã SP | Tên SP | Mô tả | Tên Thương hiệu | Tên Danh mục | Trạng thái
     * |
     * Tích điểm | Tên đơn vị | Tỷ lệ quy đổi | Đơn vị cơ bản | Mã vạch
     */
    private ProductCreateRequest parseRowToProduct(Row row, int rowNum) {
        try {
            String productCode = getCellValueAsString(row.getCell(0));
            String name = getCellValueAsString(row.getCell(1));

            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Tên sản phẩm không được để trống");
            }

            String description = getCellValueAsString(row.getCell(2));
            String brandName = getCellValueAsString(row.getCell(3));
            String categoryPath = getCellValueAsString(row.getCell(4));

            if (categoryPath == null || categoryPath.trim().isEmpty()) {
                throw new RuntimeException("Tên danh mục không được để trống");
            }

            // Tìm hoặc tạo thương hiệu theo tên
            Integer brandId = null;
            if (brandName != null && !brandName.trim().isEmpty()) {
                brandId = findOrCreateBrandByName(brandName.trim());
            }

            // Tìm hoặc tạo danh mục theo đường dẫn
            Integer categoryId = findOrCreateCategoryByPath(categoryPath.trim());

            Boolean isActive = getCellValueAsBoolean(row.getCell(5));
            Boolean isRewardPoint = getCellValueAsBoolean(row.getCell(6));

            // Parse unit information
            ProductUnitRequest unitRequest = parseUnitFromRow(row, rowNum);
            if (unitRequest == null) {
                throw new RuntimeException("Thông tin đơn vị không hợp lệ");
            }

            // Tạo ProductCreateRequest
            ProductCreateRequest product = new ProductCreateRequest();
            product.setCode(productCode != null ? productCode.trim() : null);
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
     * Parse thông tin đơn vị từ một dòng Excel
     * Format: ... | Tên đơn vị | Tỷ lệ quy đổi | Đơn vị cơ bản | Mã vạch
     */
    private ProductUnitRequest parseUnitFromRow(Row row, int rowNum) {
        try {
            String unitName = getCellValueAsString(row.getCell(7));
            Integer conversionValue = getCellValueAsInteger(row.getCell(8));
            Boolean isBaseUnit = getCellValueAsBoolean(row.getCell(9));
            String barcode = getCellValueAsString(row.getCell(10));

            if (unitName == null || unitName.trim().isEmpty()) {
                throw new RuntimeException("Tên đơn vị không được để trống");
            }
            if (conversionValue == null || conversionValue <= 0) {
                conversionValue = 1;
            }
            if (isBaseUnit == null) {
                isBaseUnit = false;
            }

            // Tạo ProductUnitRequest
            return new ProductUnitRequest(
                    unitName.trim(),
                    conversionValue,
                    isBaseUnit,
                    barcode != null ? barcode.trim() : null);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse thông tin đơn vị: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra dòng có trống không
     */
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

    /**
     * Lấy giá trị cell dưới dạng String
     */
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

    /**
     * Lấy giá trị cell dưới dạng Integer
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null)
            return null;

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
        if (cell == null)
            return null;

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

            // Tạo các dòng ví dụ
            // Ví dụ 1: Sản phẩm với 1 đơn vị
            Row exampleRow1 = sheet.createRow(1);
            String[] exampleData1 = {
                    "SP001", // Mã sản phẩm
                    "Coca Cola 330ml", // Tên sản phẩm
                    "Nước ngọt có gas", // Mô tả
                    "Coca Cola", // Tên Thương hiệu (tự động tạo nếu chưa có)
                    "Đồ uống>Nước ngọt", // Tên Danh mục (format: Cha>Con, tối đa 2 cấp)
                    "true", // Trạng thái (true/false hoặc Hoạt động/Không hoạt động)
                    "false", // Tích điểm (true/false hoặc Có/Không)
                    "Chai", // Tên đơn vị
                    "1", // Tỷ lệ quy đổi
                    "true", // Đơn vị cơ bản (true/false)
                    "1234567890123" // Mã vạch
            };

            for (int i = 0; i < exampleData1.length; i++) {
                Cell cell = exampleRow1.createCell(i);
                cell.setCellValue(exampleData1[i]);
                cell.setCellStyle(exampleStyle);
            }

            // Ví dụ 2: Sản phẩm với nhiều đơn vị (đơn vị thứ 2)
            Row exampleRow2 = sheet.createRow(2);
            String[] exampleData2 = {
                    "SP001", // Mã sản phẩm (giống ví dụ 1)
                    "Coca Cola 330ml", // Tên sản phẩm (giống ví dụ 1)
                    "Nước ngọt có gas", // Mô tả
                    "Coca Cola", // Tên Thương hiệu
                    "Đồ uống>Nước ngọt", // Tên Danh mục
                    "true", // Trạng thái
                    "false", // Tích điểm
                    "Thùng", // Tên đơn vị (khác ví dụ 1)
                    "24", // Tỷ lệ quy đổi (1 thùng = 24 chai)
                    "false", // Đơn vị cơ bản
                    "9876543210987" // Mã vạch
            };

            for (int i = 0; i < exampleData2.length; i++) {
                Cell cell = exampleRow2.createCell(i);
                cell.setCellValue(exampleData2[i]);
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
                "1. Mã sản phẩm: Tùy chọn, mã định danh sản phẩm (nếu để trống sẽ tự động sinh)",
                "2. Tên sản phẩm: Bắt buộc, không được để trống",
                "3. Mô tả: Tùy chọn, mô tả chi tiết về sản phẩm",
                "4. Tên Thương hiệu: Tùy chọn, tên thương hiệu (tự động tạo nếu chưa có)",
                "5. Tên Danh mục: Bắt buộc, tên danh mục theo format 'Cha>Con' (tối đa 2 cấp)",
                "   - Ví dụ: 'Đồ uống>Nước ngọt' hoặc 'Lương thực>Gạo'",
                "   - Nếu danh mục chưa tồn tại, hệ thống sẽ tự động tạo mới",
                "   - Có thể chỉ dùng tên đơn giản: 'Nước ngọt' (danh mục gốc, không có cha)",
                "6. Trạng thái: true/false hoặc Hoạt động/Không hoạt động (mặc định: true)",
                "7. Tích điểm: true/false hoặc Có/Không (mặc định: false)",
                "8. Tên đơn vị: Bắt buộc, tên đơn vị tính (VD: Chai, Hộp, Kg)",
                "9. Tỷ lệ quy đổi: Bắt buộc, tỷ lệ quy đổi so với đơn vị cơ bản (số nguyên > 0)",
                "10. Đơn vị cơ bản: true/false, đánh dấu đây có phải đơn vị cơ bản không",
                "11. Mã vạch: Tùy chọn, mã vạch của sản phẩm",
                "",
                "HƯỚNG DẪN TỰ ĐỘNG TẠO THƯƠNG HIỆU:",
                "- Chỉ cần nhập tên thương hiệu (VD: 'Coca Cola', 'Pepsi', 'Vinamilk')",
                "- Nếu thương hiệu chưa tồn tại, hệ thống sẽ tự động tạo mới",
                "- Nếu đã tồn tại, hệ thống sẽ sử dụng thương hiệu có sẵn",
                "- Mã thương hiệu sẽ được tự động sinh (format: BRxxxx)",
                "",
                "HƯỚNG DẪN TẠO DANH MỤC TỰ ĐỘNG (TỐI ĐA 2 CẤP):",
                "- Sử dụng ký tự '>' để phân cấp danh mục (chỉ hỗ trợ Cha>Con)",
                "- Ví dụ 1: 'Đồ uống>Nước ngọt' sẽ tạo:",
                "  + Danh mục cha: 'Đồ uống'",
                "  + Danh mục con: 'Nước ngọt' (thuộc 'Đồ uống')",
                "- Ví dụ 2: 'Nước ngọt' sẽ tạo danh mục gốc (không có cha)",
                "- Nếu danh mục đã tồn tại, hệ thống sẽ sử dụng danh mục có sẵn",
                "- Không phân biệt chữ hoa/thường khi tìm danh mục",
                "- KHÔNG hỗ trợ quá 2 cấp (VD: 'A>B>C' sẽ báo lỗi)",
                "",
                "HƯỚNG DẪN IMPORT NHIỀU ĐƠN VỊ CHO CÙNG SẢN PHẨM:",
                "- Để thêm nhiều đơn vị cho cùng một sản phẩm, tạo nhiều dòng với cùng Mã sản phẩm",
                "- Ví dụ: Coca Cola có 2 đơn vị (Chai và Thùng)",
                "  + Dòng 1: SP001 | Coca Cola | ... | Chai | 1 | true | ...",
                "  + Dòng 2: SP001 | Coca Cola | ... | Thùng | 24 | false | ...",
                "- Mỗi sản phẩm phải có ít nhất 1 đơn vị cơ bản (Đơn vị cơ bản = true)",
                "",
                "LƯU Ý:",
                "- Dòng đầu tiên là header, không được xóa hoặc sửa đổi",
                "- Dữ liệu bắt đầu từ dòng thứ 2",
                "- Các trường bắt buộc không được để trống",
                "- File phải có định dạng .xlsx hoặc .xls",
                "- Nếu có lỗi, hệ thống sẽ báo cáo chi tiết dòng bị lỗi",
                "- Mã vạch phải là duy nhất trong toàn hệ thống"
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

    /**
     * Lấy đường dẫn đầy đủ của danh mục theo format "Cha>Con"
     * Ví dụ: "Thực phẩm>Đồ uống>Nước ngọt"
     */
    private String getCategoryFullPath(ProductResponse.CategoryInfo category) {
        if (category == null) {
            return "";
        }

        // Load category entity để lấy thông tin parent
        iuh.fit.supermarket.entity.Category categoryEntity = categoryRepository.findById(category.getCategoryId())
                .orElse(null);

        if (categoryEntity == null) {
            return category.getName();
        }

        return buildCategoryPath(categoryEntity);
    }

    /**
     * Xây dựng đường dẫn danh mục từ entity
     */
    private String buildCategoryPath(iuh.fit.supermarket.entity.Category category) {
        if (category == null) {
            return "";
        }

        List<String> pathParts = new ArrayList<>();
        iuh.fit.supermarket.entity.Category current = category;

        // Duyệt ngược từ con lên cha
        while (current != null) {
            pathParts.add(0, current.getName()); // Thêm vào đầu list
            current = current.getParent();
        }

        return String.join(">", pathParts);
    }

    /**
     * Tìm hoặc tạo danh mục theo đường dẫn
     * Format: "Cha>Con" (chỉ hỗ trợ tối đa 2 cấp)
     * Nếu danh mục chưa tồn tại, sẽ tự động tạo mới
     */
    private Integer findOrCreateCategoryByPath(String categoryPath) {
        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            throw new RuntimeException("Đường dẫn danh mục không được để trống");
        }

        // Tách đường dẫn thành các phần
        String[] parts = categoryPath.split(">");

        // Kiểm tra chỉ cho phép tối đa 2 cấp
        if (parts.length > 2) {
            throw new RuntimeException(
                    "Chỉ hỗ trợ tối đa 2 cấp danh mục (Cha>Con). Đường dẫn không hợp lệ: " + categoryPath);
        }

        iuh.fit.supermarket.entity.Category parentCategory = null;

        // Duyệt qua từng phần để tìm hoặc tạo danh mục
        for (int i = 0; i < parts.length; i++) {
            String categoryName = parts[i].trim();

            if (categoryName.isEmpty()) {
                throw new RuntimeException("Tên danh mục không được để trống trong đường dẫn: " + categoryPath);
            }

            // Tìm danh mục theo tên và parent
            iuh.fit.supermarket.entity.Category category = findCategoryByNameAndParent(categoryName, parentCategory);

            if (category == null) {
                // Tạo danh mục mới
                category = new iuh.fit.supermarket.entity.Category();
                category.setName(categoryName);
                category.setDescription("Tự động tạo từ import Excel");
                category.setIsActive(true);
                category.setParent(parentCategory);

                category = categoryRepository.save(category);
                log.info("Đã tạo danh mục mới: {} (Parent: {})",
                        categoryName,
                        parentCategory != null ? parentCategory.getName() : "null");
            }

            // Cập nhật parent cho lần lặp tiếp theo
            parentCategory = category;
        }

        // Trả về ID của danh mục cuối cùng (danh mục lá)
        return parentCategory != null ? parentCategory.getCategoryId() : null;
    }

    /**
     * Tìm hoặc tạo thương hiệu theo tên
     * Nếu thương hiệu chưa tồn tại, sẽ tự động tạo mới
     */
    private Integer findOrCreateBrandByName(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            return null;
        }

        // Tìm thương hiệu theo tên (không phân biệt chữ hoa/thường)
        Optional<iuh.fit.supermarket.entity.Brand> existingBrand = brandRepository.findByName(brandName.trim());

        if (existingBrand.isPresent()) {
            return existingBrand.get().getBrandId();
        }

        // Tạo thương hiệu mới
        iuh.fit.supermarket.entity.Brand newBrand = new iuh.fit.supermarket.entity.Brand();
        newBrand.setName(brandName.trim());
        newBrand.setDescription("Tự động tạo từ import Excel");
        newBrand.setIsActive(true);

        // Tự động sinh mã thương hiệu
        String brandCode = generateBrandCode();
        newBrand.setBrandCode(brandCode);

        iuh.fit.supermarket.entity.Brand savedBrand = brandRepository.save(newBrand);
        log.info("Đã tạo thương hiệu mới: {} với mã: {}", brandName, brandCode);

        return savedBrand.getBrandId();
    }

    /**
     * Sinh mã thương hiệu tự động theo format BRxxxx
     */
    private String generateBrandCode() {
        List<String> existingCodes = brandRepository.findMaxBrandCode();

        if (existingCodes.isEmpty()) {
            return "BR0001";
        }

        String maxCode = existingCodes.get(0);
        try {
            int nextNumber = Integer.parseInt(maxCode.substring(2)) + 1;
            return String.format("BR%04d", nextNumber);
        } catch (Exception e) {
            // Nếu có lỗi, tìm số lớn nhất từ tất cả các mã
            int maxNumber = 0;
            for (String code : existingCodes) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (Exception ex) {
                    // Bỏ qua mã không hợp lệ
                }
            }
            return String.format("BR%04d", maxNumber + 1);
        }
    }

    /**
     * Tìm danh mục theo tên và parent
     */
    private iuh.fit.supermarket.entity.Category findCategoryByNameAndParent(
            String name,
            iuh.fit.supermarket.entity.Category parent) {

        if (parent == null) {
            // Tìm danh mục gốc (không có parent)
            return categoryRepository.findByParentIsNull().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        } else {
            // Tìm danh mục con của parent
            return categoryRepository.findByParent_CategoryId(parent.getCategoryId()).stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
    }
}
