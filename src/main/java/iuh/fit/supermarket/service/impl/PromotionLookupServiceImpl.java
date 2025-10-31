package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.repository.PromotionHeaderRepository;
import iuh.fit.supermarket.service.PromotionLookupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation của PromotionLookupService
 * Cung cấp thông tin khuyến mãi cho AI chat
 */
@Service
@Transactional(readOnly = true)
public class PromotionLookupServiceImpl implements PromotionLookupService {

    private final PromotionHeaderRepository promotionRepository;
    private final DateTimeFormatter dateFormatter;

    public PromotionLookupServiceImpl(PromotionHeaderRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    /**
     * Lấy khuyến mãi đang có
     */
    @Override
    public String getActivePromotions(int limit) {
        List<PromotionHeader> promotions = promotionRepository
                .findActivePromotions(LocalDateTime.now(), PageRequest.of(0, limit))
                .getContent();

        if (promotions.isEmpty()) {
            return "Hiện tại không có chương trình khuyến mãi nào đang diễn ra.";
        }

        StringBuilder result = new StringBuilder("Khuyến mãi đang có:\n\n");
        for (int i = 0; i < promotions.size(); i++) {
            PromotionHeader promo = promotions.get(i);
            result.append(String.format("%d. %s\n", i + 1, promo.getPromotionName()));
            
            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                result.append(String.format("   Mô tả: %s\n", promo.getDescription()));
            }
            
            result.append(String.format("   Thời gian: %s - %s\n",
                    promo.getStartDate().format(dateFormatter),
                    promo.getEndDate().format(dateFormatter)
            ));
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Tìm kiếm khuyến mãi
     */
    @Override
    public String searchPromotions(String keyword) {
        List<PromotionHeader> promotions = promotionRepository
                .findByKeyword(keyword, PageRequest.of(0, 5))
                .getContent();

        if (promotions.isEmpty()) {
            return "Không tìm thấy chương trình khuyến mãi nào với từ khóa: " + keyword;
        }

        StringBuilder result = new StringBuilder("Kết quả tìm kiếm khuyến mãi:\n\n");
        for (int i = 0; i < promotions.size(); i++) {
            PromotionHeader promo = promotions.get(i);
            result.append(String.format("%d. %s\n", i + 1, promo.getPromotionName()));
            
            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                result.append(String.format("   %s\n", promo.getDescription()));
            }
            
            result.append(String.format("   Trạng thái: %s\n", translateStatus(promo.getStatus().name())));
            result.append(String.format("   Thời gian: %s - %s\n",
                    promo.getStartDate().format(dateFormatter),
                    promo.getEndDate().format(dateFormatter)
            ));
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Dịch trạng thái khuyến mãi sang tiếng Việt
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "ACTIVE" -> "Đang diễn ra";
            case "UPCOMING" -> "Sắp diễn ra";
            case "EXPIRED" -> "Đã hết hạn";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
