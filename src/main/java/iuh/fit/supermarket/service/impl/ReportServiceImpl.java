package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.report.EmployeeDailySalesDTO;
import iuh.fit.supermarket.dto.report.EmployeeSalesSummaryDTO;
import iuh.fit.supermarket.dto.report.SalesDailyReportRequestDTO;
import iuh.fit.supermarket.dto.report.SalesDailyReportResponseDTO;
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
}
