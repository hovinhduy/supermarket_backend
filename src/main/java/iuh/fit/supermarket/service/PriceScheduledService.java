package iuh.fit.supermarket.service;

import iuh.fit.supermarket.service.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service cho các tác vụ định kỳ liên quan đến bảng giá
 * Tự động cập nhật trạng thái bảng giá theo thời gian
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceScheduledService {

    private final PriceService priceService;

    /**
     * Tự động cập nhật trạng thái bảng giá mỗi phút
     * - Chuyển UPCOMING sang CURRENT khi đến thời gian bắt đầu
     * - Chuyển CURRENT sang EXPIRED khi hết hạn
     */
    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây (1 phút)
    public void autoUpdatePriceStatus() {
        log.debug("Bắt đầu tác vụ tự động cập nhật trạng thái bảng giá");
        
        try {
            priceService.autoUpdatePriceStatus();
        } catch (Exception e) {
            log.error("Lỗi trong tác vụ tự động cập nhật trạng thái bảng giá: ", e);
        }
        
        log.debug("Hoàn thành tác vụ tự động cập nhật trạng thái bảng giá");
    }

    /**
     * Tự động cập nhật trạng thái bảng giá mỗi 5 phút (backup)
     * Đảm bảo không bỏ sót trường hợp nào
     */
    @Scheduled(fixedRate = 300000) // Chạy mỗi 300 giây (5 phút)
    public void autoUpdatePriceStatusBackup() {
        log.debug("Bắt đầu tác vụ backup tự động cập nhật trạng thái bảng giá");
        
        try {
            priceService.autoUpdatePriceStatus();
        } catch (Exception e) {
            log.error("Lỗi trong tác vụ backup tự động cập nhật trạng thái bảng giá: ", e);
        }
        
        log.debug("Hoàn thành tác vụ backup tự động cập nhật trạng thái bảng giá");
    }

    /**
     * Tự động cập nhật trạng thái bảng giá vào đầu mỗi giờ
     * Sử dụng cron expression để chạy vào phút 0 của mỗi giờ
     */
    @Scheduled(cron = "0 0 * * * *") // Chạy vào phút 0 của mỗi giờ
    public void autoUpdatePriceStatusHourly() {
        log.info("Bắt đầu tác vụ hàng giờ tự động cập nhật trạng thái bảng giá");
        
        try {
            priceService.autoUpdatePriceStatus();
        } catch (Exception e) {
            log.error("Lỗi trong tác vụ hàng giờ tự động cập nhật trạng thái bảng giá: ", e);
        }
        
        log.info("Hoàn thành tác vụ hàng giờ tự động cập nhật trạng thái bảng giá");
    }
}
