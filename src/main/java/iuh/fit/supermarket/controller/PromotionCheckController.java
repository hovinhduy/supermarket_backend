package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.checkout.CheckPromotionRequestDTO;
import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.PromotionCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến kiểm tra và áp dụng khuyến mãi
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionCheckController {

    private final PromotionCheckService promotionCheckService;

    /**
     * API kiểm tra và áp dụng khuyến mãi cho giỏ hàng
     * 
     * @param request danh sách sản phẩm trong giỏ hàng
     * @return response với các sản phẩm đã áp dụng khuyến mãi và tổng hợp
     */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<CheckPromotionResponseDTO>> checkPromotions(
            @Valid @RequestBody CheckPromotionRequestDTO request) {
        
        log.info("Nhận yêu cầu kiểm tra khuyến mãi cho {} sản phẩm", request.items().size());

        try {
            CheckPromotionResponseDTO response = promotionCheckService.checkAndApplyPromotions(request);
            
            log.info("Kiểm tra khuyến mãi thành công. Tổng {} dòng sản phẩm (bao gồm quà tặng)", 
                    response.items().size());
            
            return ResponseEntity.ok(ApiResponse.success("Áp dụng khuyến mãi thành công", response));
            
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra khuyến mãi: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi kiểm tra khuyến mãi: " + e.getMessage()));
        }
    }
}
