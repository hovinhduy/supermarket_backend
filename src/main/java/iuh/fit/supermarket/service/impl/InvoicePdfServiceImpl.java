package iuh.fit.supermarket.service.impl;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import iuh.fit.supermarket.dto.sale.AppliedOrderPromotionDetailDTO;
import iuh.fit.supermarket.dto.sale.SaleInvoiceFullDTO;
import iuh.fit.supermarket.dto.sale.SaleInvoiceItemDetailDTO;
import iuh.fit.supermarket.service.InvoicePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Implementation của InvoicePdfService để tạo PDF hóa đơn
 */
@Service
@Slf4j
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    public byte[] generateInvoicePdf(SaleInvoiceFullDTO invoice) {
        log.info("Bắt đầu tạo PDF cho hóa đơn: {}", invoice.invoiceNumber());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Set font hỗ trợ tiếng Việt - sử dụng font từ hệ thống hoặc font embedded
            PdfFont font = getVietnameseFont();
            document.setFont(font);

            // Header - Thông tin cửa hàng
            addHeader(document);

            // Thông tin hóa đơn
            addInvoiceInfo(document, invoice);

            // Thông tin khách hàng và nhân viên
            addCustomerAndEmployeeInfo(document, invoice);

            // Bảng chi tiết sản phẩm
            addItemsTable(document, invoice);

            // Thông tin khuyến mãi order-level
            if (invoice.appliedOrderPromotions() != null && !invoice.appliedOrderPromotions().isEmpty()) {
                addOrderPromotions(document, invoice);
            }

            // Tổng kết
            addTotalSection(document, invoice);

            // Footer
            addFooter(document);

            document.close();

            log.info("Tạo PDF thành công cho hóa đơn: {}", invoice.invoiceNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Lỗi khi tạo PDF cho hóa đơn: {}", invoice.invoiceNumber(), e);
            throw new RuntimeException("Không thể tạo PDF hóa đơn", e);
        }
    }

    private void addHeader(Document document) throws Exception {
        Paragraph title = new Paragraph("SIÊU THỊ MINI")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("HÓA ĐƠN BÁN HÀNG")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitle);

        Paragraph address = new Paragraph("Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP.HCM")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(address);

        Paragraph contact = new Paragraph("Điện thoại: 0123456789 | Email: contact@supermarket.vn")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(contact);
    }

    private void addInvoiceInfo(Document document, SaleInvoiceFullDTO invoice) {
        Table infoTable = new Table(2);
        infoTable.setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(createCell("Số hóa đơn:", false));
        infoTable.addCell(createCell(invoice.invoiceNumber(), true));

        infoTable.addCell(createCell("Ngày lập:", false));
        infoTable.addCell(createCell(invoice.invoiceDate().format(DATE_FORMATTER), true));

        infoTable.addCell(createCell("Trạng thái:", false));
        infoTable.addCell(createCell(getStatusText(invoice.status()), true));

        infoTable.addCell(createCell("Phương thức thanh toán:", false));
        infoTable.addCell(createCell(getPaymentMethodText(invoice.paymentMethod()), true));

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addCustomerAndEmployeeInfo(Document document, SaleInvoiceFullDTO invoice) {
        Table customerTable = new Table(2);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        customerTable.addCell(createCell("Khách hàng:", false));
        customerTable.addCell(createCell(
                invoice.customerName() != null ? invoice.customerName() : "Khách lẻ", true));

        customerTable.addCell(createCell("Nhân viên:", false));
        customerTable.addCell(createCell(invoice.employeeName(), true));

        document.add(customerTable);
        document.add(new Paragraph("\n"));
    }

    private void addItemsTable(Document document, SaleInvoiceFullDTO invoice) {
        Table itemsTable = new Table(new float[] { 1, 4, 2, 2, 2, 3 });
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Header của bảng
        DeviceRgb headerColor = new DeviceRgb(200, 200, 200);
        itemsTable.addHeaderCell(createHeaderCell("STT", headerColor));
        itemsTable.addHeaderCell(createHeaderCell("Sản phẩm", headerColor));
        itemsTable.addHeaderCell(createHeaderCell("ĐVT", headerColor));
        itemsTable.addHeaderCell(createHeaderCell("SL", headerColor));
        itemsTable.addHeaderCell(createHeaderCell("Đơn giá", headerColor));
        itemsTable.addHeaderCell(createHeaderCell("Thành tiền", headerColor));

        // Dữ liệu
        int index = 1;
        for (SaleInvoiceItemDetailDTO item : invoice.items()) {
            itemsTable.addCell(createCell(String.valueOf(index++), false));
            itemsTable.addCell(createCell(item.productName(), false));
            itemsTable.addCell(createCell(item.unit(), false));
            itemsTable.addCell(createCell(String.valueOf(item.quantity()), false));
            itemsTable.addCell(createCell(CURRENCY_FORMATTER.format(item.unitPrice()), false));
            itemsTable.addCell(createCell(CURRENCY_FORMATTER.format(item.lineTotal()), false));

            // Hiển thị khuyến mãi của item (nếu có)
            if (item.appliedPromotions() != null && !item.appliedPromotions().isEmpty()) {
                Cell promotionCell = new Cell(1, 6);
                promotionCell.setBorder(Border.NO_BORDER);
                promotionCell.setFontSize(9);
                promotionCell.setItalic();
                promotionCell.setFontColor(ColorConstants.BLUE);

                StringBuilder promoText = new StringBuilder("   Khuyến mãi: ");
                for (var promo : item.appliedPromotions()) {
                    promoText.append(promo.promotionSummary())
                            .append(" (-")
                            .append(CURRENCY_FORMATTER.format(promo.discountValue()))
                            .append("); ");
                }

                promotionCell.add(new Paragraph(promoText.toString()));
                itemsTable.addCell(promotionCell);
            }
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n"));
    }

    private void addOrderPromotions(Document document, SaleInvoiceFullDTO invoice) {
        Paragraph promoTitle = new Paragraph("KHUYẾN MÃI ĐƠN HÀNG")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(5);
        document.add(promoTitle);

        for (AppliedOrderPromotionDetailDTO promo : invoice.appliedOrderPromotions()) {
            Paragraph promoDetail = new Paragraph(
                    String.format("  • %s: -%s",
                            promo.promotionSummary(),
                            CURRENCY_FORMATTER.format(promo.discountValue())))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginLeft(10);
            document.add(promoDetail);
        }

        document.add(new Paragraph("\n"));
    }

    private void addTotalSection(Document document, SaleInvoiceFullDTO invoice) {
        Table totalTable = new Table(2);
        totalTable.setWidth(UnitValue.createPercentValue(50));
        totalTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        totalTable.addCell(createCell("Tổng tiền hàng:", false));
        totalTable.addCell(createCell(CURRENCY_FORMATTER.format(invoice.subtotal()), true));

        totalTable.addCell(createCell("Tổng giảm giá:", false));
        totalTable.addCell(createCell("-" + CURRENCY_FORMATTER.format(invoice.totalDiscount()), true));

        totalTable.addCell(createCell("Thuế:", false));
        totalTable.addCell(createCell(CURRENCY_FORMATTER.format(invoice.totalTax()), true));

        Cell totalLabelCell = new Cell()
                .add(new Paragraph("TỔNG THANH TOÁN:"))
                .setBold()
                .setFontSize(12)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        totalTable.addCell(totalLabelCell);

        Cell totalValueCell = new Cell()
                .add(new Paragraph(CURRENCY_FORMATTER.format(invoice.totalAmount())))
                .setBold()
                .setFontSize(12)
                .setFontColor(ColorConstants.RED)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        totalTable.addCell(totalValueCell);

        totalTable.addCell(createCell("Đã thanh toán:", false));
        totalTable.addCell(createCell(CURRENCY_FORMATTER.format(invoice.paidAmount()), true));

        BigDecimal remaining = invoice.totalAmount().subtract(invoice.paidAmount());
        totalTable.addCell(createCell("Còn lại:", false));
        totalTable.addCell(createCell(CURRENCY_FORMATTER.format(remaining), true));

        document.add(totalTable);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));

        Paragraph thankYou = new Paragraph("Cảm ơn quý khách đã mua hàng!")
                .setFontSize(11)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(thankYou);

        Paragraph note = new Paragraph("Vui lòng giữ hóa đơn để đổi trả hàng trong vòng 7 ngày")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(note);
    }

    private Cell createCell(String content, boolean bold) {
        Cell cell = new Cell();
        Paragraph para = new Paragraph(content);
        if (bold) {
            para.setBold();
        }
        cell.add(para);
        cell.setBorder(Border.NO_BORDER);
        cell.setFontSize(10);
        return cell;
    }

    private Cell createHeaderCell(String content, DeviceRgb backgroundColor) {
        Cell cell = new Cell();
        cell.add(new Paragraph(content).setBold());
        cell.setBackgroundColor(backgroundColor);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setFontSize(10);
        return cell;
    }

    private String getStatusText(iuh.fit.supermarket.enums.InvoiceStatus status) {
        return switch (status) {
            case PAID -> "Đã thanh toán";
            case UNPAID -> "Chưa thanh toán";
            default -> status.name();
        };
    }

    private String getPaymentMethodText(iuh.fit.supermarket.enums.PaymentMethod method) {
        return switch (method) {
            case CASH -> "Tiền mặt";
            case CARD -> "Thẻ";
            case ONLINE -> "Chuyển khoản";
            default -> method.name();
        };
    }

    /**
     * Tạo font hỗ trợ tiếng Việt
     * Thử các font từ hệ thống Windows/Linux, fallback về Helvetica với Unicode nếu
     * không tìm thấy
     */
    private PdfFont getVietnameseFont() {
        try {
            // Thử tìm font Arial trên Windows
            try {
                String arialPath = "C:/Windows/Fonts/arial.ttf";
                if (new java.io.File(arialPath).exists()) {
                    return PdfFontFactory.createFont(arialPath, PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                }
            } catch (Exception e) {
                log.debug("Không tìm thấy font Arial trên Windows");
            }

            // Thử tìm font trên Linux
            try {
                String[] linuxFonts = {
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
                };

                for (String fontPath : linuxFonts) {
                    if (new java.io.File(fontPath).exists()) {
                        return PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
            } catch (Exception e) {
                log.debug("Không tìm thấy font trên Linux");
            }

            // Fallback: Sử dụng Helvetica với encoding Unicode
            log.warn("Không tìm thấy font hệ thống, sử dụng Helvetica (có thể không hiển thị đúng tiếng Việt)");
            return PdfFontFactory.createFont(StandardFonts.HELVETICA,
                    PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

        } catch (Exception e) {
            log.error("Lỗi khi tạo font: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo font cho PDF", e);
        }
    }
}
