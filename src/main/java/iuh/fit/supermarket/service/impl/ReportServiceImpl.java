package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.report.*;
import iuh.fit.supermarket.repository.SaleInvoiceHeaderRepository;
import iuh.fit.supermarket.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
}
