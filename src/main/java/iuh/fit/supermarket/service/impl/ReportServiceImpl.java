package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.report.*;
import iuh.fit.supermarket.repository.ReturnInvoiceHeaderRepository;
import iuh.fit.supermarket.repository.SaleInvoiceHeaderRepository;
import iuh.fit.supermarket.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của ReportService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final ReturnInvoiceHeaderRepository returnInvoiceHeaderRepository;

    @Override
    @Transactional(readOnly = true)
    public SalesDailyReportResponseDTO getSalesDailyReport(SalesDailyReportRequestDTO request) {
        log.info("Lấy báo cáo doanh số từ {} đến {}, nhân viên: {}",
                request.fromDate(), request.toDate(), request.employeeId());

        List<Object[]> rawResults = saleInvoiceHeaderRepository.getSalesDailyReportRaw(
                request.fromDate(),
                request.toDate(),
                request.employeeId()
        );

        List<EmployeeDailySalesDTO> dailySales = rawResults.stream()
                .map(row -> new EmployeeDailySalesDTO(
                        (String) row[0],
                        (String) row[1],
                        ((java.time.LocalDateTime) row[2]).toLocalDate(),
                        (BigDecimal) row[3],
                        (BigDecimal) row[4],
                        (BigDecimal) row[5]
                ))
                .toList();

        log.info("Tìm thấy {} bản ghi doanh số", dailySales.size());

        Map<String, List<EmployeeDailySalesDTO>> groupedByEmployee = new LinkedHashMap<>();
        for (EmployeeDailySalesDTO sale : dailySales) {
            groupedByEmployee
                    .computeIfAbsent(sale.employeeCode(), k -> new ArrayList<>())
                    .add(sale);
        }

        List<EmployeeSalesSummaryDTO> employeeSalesList = new ArrayList<>();
        int stt = 1;
        BigDecimal grandTotalDiscount = BigDecimal.ZERO;
        BigDecimal grandTotalRevenueBeforeDiscount = BigDecimal.ZERO;
        BigDecimal grandTotalRevenueAfterDiscount = BigDecimal.ZERO;

        for (Map.Entry<String, List<EmployeeDailySalesDTO>> entry : groupedByEmployee.entrySet()) {
            List<EmployeeDailySalesDTO> employeeDailySales = entry.getValue();

            String employeeCode = employeeDailySales.get(0).employeeCode();
            String employeeName = employeeDailySales.get(0).employeeName();

            BigDecimal totalDiscount = employeeDailySales.stream()
                    .map(EmployeeDailySalesDTO::totalDiscount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRevenueBeforeDiscount = employeeDailySales.stream()
                    .map(EmployeeDailySalesDTO::revenueBeforeDiscount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRevenueAfterDiscount = employeeDailySales.stream()
                    .map(EmployeeDailySalesDTO::revenueAfterDiscount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            EmployeeSalesSummaryDTO summary = new EmployeeSalesSummaryDTO(
                    stt++,
                    employeeCode,
                    employeeName,
                    employeeDailySales,
                    totalDiscount,
                    totalRevenueBeforeDiscount,
                    totalRevenueAfterDiscount
            );

            employeeSalesList.add(summary);

            grandTotalDiscount = grandTotalDiscount.add(totalDiscount);
            grandTotalRevenueBeforeDiscount = grandTotalRevenueBeforeDiscount.add(totalRevenueBeforeDiscount);
            grandTotalRevenueAfterDiscount = grandTotalRevenueAfterDiscount.add(totalRevenueAfterDiscount);
        }

        log.info("Tổng hợp báo cáo: {} nhân viên, tổng doanh số: {}",
                employeeSalesList.size(), grandTotalRevenueAfterDiscount);

        return new SalesDailyReportResponseDTO(
                request.fromDate(),
                request.toDate(),
                employeeSalesList,
                grandTotalDiscount,
                grandTotalRevenueBeforeDiscount,
                grandTotalRevenueAfterDiscount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSalesReportResponseDTO getCustomerSalesReport(CustomerSalesReportRequestDTO request) {
        log.info("Lấy báo cáo doanh số theo khách hàng từ {} đến {}, khách hàng ID: {}",
                request.fromDate(), request.toDate(), request.customerId());

        // Lấy dữ liệu thô từ repository
        List<CustomerCategorySalesProjection> rawResults = saleInvoiceHeaderRepository.findCustomerSalesReport(
                request.fromDate(),
                request.toDate(),
                request.customerId()
        );

        log.info("Tìm thấy {} bản ghi doanh số theo khách hàng và nhóm sản phẩm", rawResults.size());

        // Nhóm dữ liệu theo customer
        Map<Integer, List<CustomerCategorySalesProjection>> groupedByCustomer = new LinkedHashMap<>();
        for (CustomerCategorySalesProjection record : rawResults) {
            groupedByCustomer
                    .computeIfAbsent(record.customerId(), k -> new ArrayList<>())
                    .add(record);
        }

        // Xây dựng danh sách CustomerSalesSummaryDTO
        List<CustomerSalesSummaryDTO> customerSalesList = new ArrayList<>();
        BigDecimal grandTotalDiscount = BigDecimal.ZERO;
        BigDecimal grandTotalRevenueBeforeDiscount = BigDecimal.ZERO;
        BigDecimal grandTotalRevenueAfterDiscount = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<CustomerCategorySalesProjection>> entry : groupedByCustomer.entrySet()) {
            List<CustomerCategorySalesProjection> customerRecords = entry.getValue();

            // Lấy thông tin khách hàng từ record đầu tiên
            CustomerCategorySalesProjection firstRecord = customerRecords.get(0);

            // Tạo danh sách CategorySalesDTO cho khách hàng này
            List<CategorySalesDTO> categorySalesList = new ArrayList<>();
            BigDecimal customerTotalDiscount = BigDecimal.ZERO;
            BigDecimal customerTotalRevenueBeforeDiscount = BigDecimal.ZERO;
            BigDecimal customerTotalRevenueAfterDiscount = BigDecimal.ZERO;

            for (CustomerCategorySalesProjection record : customerRecords) {
                CategorySalesDTO categorySales = new CategorySalesDTO(
                        record.categoryName() != null ? record.categoryName() : "Chưa phân loại",
                        record.revenueBeforeDiscount(),
                        record.discount(),
                        record.revenueAfterDiscount()
                );
                categorySalesList.add(categorySales);

                // Cộng dồn tổng cho khách hàng
                customerTotalDiscount = customerTotalDiscount.add(record.discount());
                customerTotalRevenueBeforeDiscount = customerTotalRevenueBeforeDiscount.add(record.revenueBeforeDiscount());
                customerTotalRevenueAfterDiscount = customerTotalRevenueAfterDiscount.add(record.revenueAfterDiscount());
            }

            // Tạo CustomerSalesSummaryDTO
            CustomerSalesSummaryDTO customerSummary = new CustomerSalesSummaryDTO(
                    firstRecord.customerId(),
                    firstRecord.customerCode(),
                    firstRecord.customerName(),
                    firstRecord.address(),
                    firstRecord.customerType(),
                    categorySalesList,
                    customerTotalDiscount,
                    customerTotalRevenueBeforeDiscount,
                    customerTotalRevenueAfterDiscount
            );

            customerSalesList.add(customerSummary);

            // Cộng dồn grand total
            grandTotalDiscount = grandTotalDiscount.add(customerTotalDiscount);
            grandTotalRevenueBeforeDiscount = grandTotalRevenueBeforeDiscount.add(customerTotalRevenueBeforeDiscount);
            grandTotalRevenueAfterDiscount = grandTotalRevenueAfterDiscount.add(customerTotalRevenueAfterDiscount);
        }

        log.info("Tổng hợp báo cáo: {} khách hàng, tổng doanh số: {}",
                customerSalesList.size(), grandTotalRevenueAfterDiscount);

        return new CustomerSalesReportResponseDTO(
                request.fromDate(),
                request.toDate(),
                customerSalesList,
                grandTotalDiscount,
                grandTotalRevenueBeforeDiscount,
                grandTotalRevenueAfterDiscount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnReportResponseDTO getReturnReport(ReturnReportRequestDTO request) {
        log.info("Lấy báo cáo trả hàng từ {} đến {}", request.fromDate(), request.toDate());

        // Lấy dữ liệu thô từ repository
        List<Object[]> rawResults = returnInvoiceHeaderRepository.getReturnReportRaw(
                request.fromDate(),
                request.toDate()
        );

        log.info("Tìm thấy {} bản ghi trả hàng", rawResults.size());

        // Group dữ liệu theo returnCode
        Map<String, List<Object[]>> groupedByReturnCode = new LinkedHashMap<>();
        for (Object[] row : rawResults) {
            String returnCode = (String) row[2];
            groupedByReturnCode.computeIfAbsent(returnCode, k -> new ArrayList<>()).add(row);
        }

        // Chuyển đổi sang DTO và tính tổng
        List<ReturnReportItemDTO> returnItems = new ArrayList<>();
        Integer grandTotalQuantity = 0;
        BigDecimal grandTotalRefundAmount = BigDecimal.ZERO;

        for (Map.Entry<String, List<Object[]>> entry : groupedByReturnCode.entrySet()) {
            List<Object[]> invoiceRows = entry.getValue();
            Object[] firstRow = invoiceRows.get(0);

            // Lấy thông tin chung của hóa đơn trả từ row đầu tiên
            String originalInvoiceNumber = (String) firstRow[0];
            LocalDate originalInvoiceDate = ((java.time.LocalDateTime) firstRow[1]).toLocalDate();
            String returnCode = (String) firstRow[2];
            LocalDate returnDate = ((java.time.LocalDateTime) firstRow[3]).toLocalDate();

            // Tạo danh sách sản phẩm và tính tổng cho hóa đơn này
            List<ReturnProductItemDTO> products = new ArrayList<>();
            Integer invoiceTotalQuantity = 0;
            BigDecimal invoiceTotalRefundAmount = BigDecimal.ZERO;

            for (Object[] row : invoiceRows) {
                String categoryName = (String) row[4];
                String productCode = (String) row[5];
                String productName = (String) row[6];
                String unitName = (String) row[7];
                Integer quantity = (Integer) row[8];
                BigDecimal priceAtReturn = (BigDecimal) row[9];
                BigDecimal refundAmount = (BigDecimal) row[10];

                ReturnProductItemDTO product = new ReturnProductItemDTO(
                        categoryName != null ? categoryName : "Chưa phân loại",
                        productCode,
                        productName,
                        unitName,
                        quantity,
                        priceAtReturn,
                        refundAmount
                );

                products.add(product);
                invoiceTotalQuantity += quantity;
                invoiceTotalRefundAmount = invoiceTotalRefundAmount.add(refundAmount);
            }

            // Tạo DTO cho hóa đơn trả
            ReturnReportItemDTO returnItem = new ReturnReportItemDTO(
                    originalInvoiceNumber,
                    originalInvoiceDate,
                    returnCode,
                    returnDate,
                    products,
                    invoiceTotalQuantity,
                    invoiceTotalRefundAmount
            );

            returnItems.add(returnItem);
            grandTotalQuantity += invoiceTotalQuantity;
            grandTotalRefundAmount = grandTotalRefundAmount.add(invoiceTotalRefundAmount);
        }

        log.info("Tổng hợp báo cáo trả hàng: {} hóa đơn trả, tổng số lượng: {}, tổng tiền hoàn: {}",
                returnItems.size(), grandTotalQuantity, grandTotalRefundAmount);

        return new ReturnReportResponseDTO(
                request.fromDate(),
                request.toDate(),
                returnItems,
                grandTotalQuantity,
                grandTotalRefundAmount
        );
    }
}
