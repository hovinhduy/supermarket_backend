package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.cart.AddCartItemRequest;
import iuh.fit.supermarket.dto.cart.CartItemResponse;
import iuh.fit.supermarket.dto.cart.CartResponse;
import iuh.fit.supermarket.dto.cart.UpdateCartItemRequest;
import iuh.fit.supermarket.dto.checkout.CartItemRequestDTO;
import iuh.fit.supermarket.dto.checkout.CartItemResponseDTO;
import iuh.fit.supermarket.dto.checkout.CheckPromotionRequestDTO;
import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;
import iuh.fit.supermarket.dto.price.PriceDetailDto;
import iuh.fit.supermarket.entity.CartItem;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.ShoppingCart;
import iuh.fit.supermarket.repository.CartItemRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.repository.ShoppingCartRepository;
import iuh.fit.supermarket.repository.WarehouseRepository;
import iuh.fit.supermarket.service.PriceService;
import iuh.fit.supermarket.service.PromotionCheckService;
import iuh.fit.supermarket.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation của ShoppingCartService
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

        private final ShoppingCartRepository shoppingCartRepository;
        private final CartItemRepository cartItemRepository;
        private final ProductUnitRepository productUnitRepository;
        private final CustomerRepository customerRepository;
        private final PriceService priceService;
        private final WarehouseRepository warehouseRepository;
        private final PromotionCheckService promotionCheckService;

        /**
         * Lấy giỏ hàng của khách hàng
         */
        @Override
        @Transactional
        public CartResponse getCart(Integer customerId) {
                log.info("Lấy giỏ hàng cho khách hàng ID: {}", customerId);

                // Tìm hoặc tạo giỏ hàng cho khách hàng
                ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                                .orElseGet(() -> createCartForCustomer(customerId));

                return buildCartResponse(cart);
        }

        /**
         * Thêm sản phẩm vào giỏ hàng
         */
        @Override
        @Transactional
        public CartResponse addItemToCart(Integer customerId, AddCartItemRequest request) {
                log.info("Thêm sản phẩm vào giỏ hàng - Customer ID: {}, Product Unit ID: {}, Quantity: {}",
                                customerId, request.productUnitId(), request.quantity());

                // Kiểm tra product unit có tồn tại không
                ProductUnit productUnit = productUnitRepository.findById(request.productUnitId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy product unit với ID: " + request.productUnitId()));

                // Kiểm tra product unit có active không
                if (!productUnit.getIsActive() || productUnit.getIsDeleted()) {
                        throw new RuntimeException("Sản phẩm này không còn khả dụng");
                }

                // Lấy giá hiện tại (chỉ lấy từ bảng giá ACTIVE có startDate <= now và endDate > now)
                PriceDetailDto currentPrice = priceService.getCurrentPriceByProductUnitId(request.productUnitId());
                if (currentPrice == null) {
                        throw new RuntimeException(
                                        "Sản phẩm này hiện không có giá hợp lệ hoặc không nằm trong bảng giá đang áp dụng");
                }

                // Lấy số lượng tồn kho
                Integer stockQuantity = warehouseRepository.findByProductUnitId(request.productUnitId())
                                .map(warehouse -> warehouse.getQuantityOnHand())
                                .orElse(0);

                // Kiểm tra sản phẩm còn hàng không
                if (stockQuantity <= 0) {
                        throw new RuntimeException("Sản phẩm này hiện đã hết hàng");
                }

                // Tìm hoặc tạo giỏ hàng
                ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                                .orElseGet(() -> createCartForCustomer(customerId));

                // Kiểm tra xem item đã có trong giỏ chưa
                cartItemRepository.findByCartIdAndProductUnitId(cart.getCartId(), request.productUnitId())
                                .ifPresentOrElse(
                                                // Nếu đã có thì cập nhật số lượng
                                                existingItem -> {
                                                        int newTotalQuantity = existingItem.getQuantity()
                                                                        + request.quantity();

                                                        // Kiểm tra số lượng tồn kho
                                                        if (newTotalQuantity > stockQuantity) {
                                                                throw new RuntimeException(
                                                                                "Không thể thêm vào giỏ hàng. Số lượng yêu cầu ("
                                                                                                + newTotalQuantity
                                                                                                + ") vượt quá số lượng tồn kho ("
                                                                                                + stockQuantity + ")");
                                                        }

                                                        existingItem.setQuantity(newTotalQuantity);
                                                        cartItemRepository.save(existingItem);
                                                        log.info("Cập nhật số lượng item trong giỏ hàng");
                                                },
                                                // Nếu chưa có thì thêm mới
                                                () -> {
                                                        // Kiểm tra số lượng tồn kho
                                                        if (request.quantity() > stockQuantity) {
                                                                throw new RuntimeException(
                                                                                "Không thể thêm vào giỏ hàng. Số lượng yêu cầu ("
                                                                                                + request.quantity()
                                                                                                + ") vượt quá số lượng tồn kho ("
                                                                                                + stockQuantity + ")");
                                                        }

                                                        CartItem newItem = new CartItem();
                                                        newItem.setCart(cart);
                                                        newItem.setProductUnit(productUnit);
                                                        newItem.setQuantity(request.quantity());
                                                        newItem.setUnitPrice(currentPrice.getSalePrice().doubleValue());
                                                        cartItemRepository.save(newItem);
                                                        log.info("Thêm item mới vào giỏ hàng");
                                                });

                return buildCartResponse(cart);
        }

        /**
         * Cập nhật số lượng sản phẩm trong giỏ hàng
         */
        @Override
        @Transactional
        public CartResponse updateCartItem(Integer customerId, Long productUnitId, UpdateCartItemRequest request) {
                log.info("Cập nhật item trong giỏ hàng - Customer ID: {}, Product Unit ID: {}, New Quantity: {}",
                                customerId, productUnitId, request.quantity());

                // Tìm giỏ hàng
                ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

                // Tìm cart item
                CartItem cartItem = cartItemRepository.findByCartIdAndProductUnitId(cart.getCartId(), productUnitId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

                // Lấy số lượng tồn kho
                Integer stockQuantity = warehouseRepository.findByProductUnitId(productUnitId)
                                .map(warehouse -> warehouse.getQuantityOnHand())
                                .orElse(0);

                // Kiểm tra sản phẩm còn hàng không
                if (stockQuantity <= 0) {
                        throw new RuntimeException("Sản phẩm này hiện đã hết hàng");
                }

                // Kiểm tra số lượng tồn kho
                if (request.quantity() > stockQuantity) {
                        throw new RuntimeException(
                                        "Không thể cập nhật số lượng. Số lượng yêu cầu ("
                                                        + request.quantity()
                                                        + ") vượt quá số lượng tồn kho ("
                                                        + stockQuantity + ")");
                }

                // Cập nhật số lượng
                cartItem.setQuantity(request.quantity());
                cartItemRepository.save(cartItem);

                log.info("Cập nhật số lượng thành công");
                return buildCartResponse(cart);
        }

        /**
         * Xóa sản phẩm khỏi giỏ hàng
         */
        @Override
        @Transactional
        public CartResponse removeItemFromCart(Integer customerId, Long productUnitId) {
                log.info("Xóa item khỏi giỏ hàng - Customer ID: {}, Product Unit ID: {}", customerId, productUnitId);

                // Tìm giỏ hàng
                ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

                // Xóa item
                cartItemRepository.deleteByCartIdAndProductUnitId(cart.getCartId(), productUnitId);

                log.info("Xóa item thành công");
                return buildCartResponse(cart);
        }

        /**
         * Xóa toàn bộ giỏ hàng
         */
        @Override
        @Transactional
        public void clearCart(Integer customerId) {
                log.info("Xóa toàn bộ giỏ hàng - Customer ID: {}", customerId);

                ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

                cartItemRepository.deleteByCartId(cart.getCartId());
                log.info("Xóa toàn bộ giỏ hàng thành công");
        }

        /**
         * Tạo giỏ hàng mới cho khách hàng
         */
        private ShoppingCart createCartForCustomer(Integer customerId) {
                log.info("Tạo giỏ hàng mới cho khách hàng ID: {}", customerId);

                Customer customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy khách hàng với ID: " + customerId));

                ShoppingCart cart = new ShoppingCart();
                cart.setCustomer(customer);
                return shoppingCartRepository.save(cart);
        }

        /**
         * Build CartResponse từ ShoppingCart entity với khuyến mãi
         */
        private CartResponse buildCartResponse(ShoppingCart cart) {
                List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());

                // Nếu giỏ hàng trống, trả về response rỗng
                if (cartItems.isEmpty()) {
                        return new CartResponse(
                                        cart.getCartId(),
                                        cart.getCustomer() != null ? cart.getCustomer().getCustomerId() : null,
                                        new ArrayList<>(),
                                        0,
                                        0.0,
                                        0.0,
                                        0.0,
                                        0.0,
                                        new ArrayList<>(),
                                        cart.getCreatedAt(),
                                        cart.getUpdatedAt());
                }

                // Tạo request để kiểm tra khuyến mãi
                List<CartItemRequestDTO> requestItems = cartItems.stream()
                                .map(item -> new CartItemRequestDTO(
                                                item.getProductUnit().getId(),
                                                item.getQuantity()))
                                .collect(Collectors.toList());

                CheckPromotionRequestDTO promotionRequest = new CheckPromotionRequestDTO(requestItems);

                // Gọi service kiểm tra khuyến mãi
                CheckPromotionResponseDTO promotionResponse = promotionCheckService
                                .checkAndApplyPromotions(promotionRequest);

                // Tạo map để lookup thông tin bổ sung (hình ảnh, tồn kho, thời gian)
                Map<Long, CartItem> cartItemMap = cartItems.stream()
                                .collect(Collectors.toMap(
                                                item -> item.getProductUnit().getId(),
                                                item -> item));

                // Convert CartItemResponseDTO sang CartItemResponse với thông tin bổ sung
                List<CartItemResponse> itemResponses = promotionResponse.items().stream()
                                .map(promoItem -> convertToCartItemResponse(promoItem, cartItemMap))
                                .collect(Collectors.toList());

                // Tính tổng số lượng (chỉ tính sản phẩm mua, không tính quà tặng)
                int totalItems = promotionResponse.items().stream()
                                .filter(item -> item.promotionApplied() == null
                                                || item.promotionApplied().sourceLineItemId() == null)
                                .mapToInt(CartItemResponseDTO::quantity)
                                .sum();

                return new CartResponse(
                                cart.getCartId(),
                                cart.getCustomer() != null ? cart.getCustomer().getCustomerId() : null,
                                itemResponses,
                                totalItems,
                                promotionResponse.summary().subTotal().doubleValue(),
                                promotionResponse.summary().lineItemDiscount().doubleValue(),
                                promotionResponse.summary().orderDiscount().doubleValue(),
                                promotionResponse.summary().totalPayable().doubleValue(),
                                promotionResponse.appliedOrderPromotions(),
                                cart.getCreatedAt(),
                                cart.getUpdatedAt());
        }

        /**
         * Convert CartItemResponseDTO từ PromotionCheckService sang CartItemResponse
         */
        private CartItemResponse convertToCartItemResponse(
                        CartItemResponseDTO promoItem,
                        Map<Long, CartItem> cartItemMap) {

                CartItem cartItem = cartItemMap.get(promoItem.productUnitId());

                String imageUrl = null;
                Integer stockQuantity = 0;
                LocalDateTime createdAt = null;
                LocalDateTime updatedAt = null;

                if (cartItem != null) {
                        ProductUnit productUnit = cartItem.getProductUnit();

                        // Lấy hình ảnh chính
                        imageUrl = productUnit.getProductUnitImages() != null
                                        && !productUnit.getProductUnitImages().isEmpty()
                                                        ? productUnit.getProductUnitImages().stream()
                                                                        .filter(img -> Boolean.TRUE
                                                                                        .equals(img.getIsPrimary()))
                                                                        .findFirst()
                                                                        .map(img -> img.getProductImage().getImageUrl())
                                                                        .orElse(null)
                                                        : null;

                        // Lấy số lượng tồn kho
                        stockQuantity = warehouseRepository.findByProductUnitId(productUnit.getId())
                                        .map(warehouse -> warehouse.getQuantityOnHand())
                                        .orElse(0);

                        createdAt = cartItem.getCreatedAt();
                        updatedAt = cartItem.getUpdatedAt();
                } else {
                        // Đây là quà tặng, lấy thông tin từ product unit
                        ProductUnit giftProductUnit = productUnitRepository.findById(promoItem.productUnitId())
                                        .orElse(null);

                        if (giftProductUnit != null) {
                                imageUrl = giftProductUnit.getProductUnitImages() != null
                                                && !giftProductUnit.getProductUnitImages().isEmpty()
                                                                ? giftProductUnit.getProductUnitImages().stream()
                                                                                .filter(img -> Boolean.TRUE
                                                                                                .equals(img.getIsPrimary()))
                                                                                .findFirst()
                                                                                .map(img -> img.getProductImage()
                                                                                                .getImageUrl())
                                                                                .orElse(null)
                                                                : null;

                                stockQuantity = warehouseRepository.findByProductUnitId(giftProductUnit.getId())
                                                .map(warehouse -> warehouse.getQuantityOnHand())
                                                .orElse(0);
                        }
                }

                return new CartItemResponse(
                                promoItem.lineItemId(),
                                promoItem.productUnitId(),
                                promoItem.productName(),
                                promoItem.unit(),
                                promoItem.quantity(),
                                promoItem.unitPrice().doubleValue(),
                                promoItem.unitPrice().multiply(BigDecimal.valueOf(promoItem.quantity())).doubleValue(),
                                promoItem.lineTotal().doubleValue(),
                                imageUrl,
                                stockQuantity,
                                promoItem.hasPromotion() != null ? promoItem.hasPromotion() : false,
                                promoItem.promotionApplied(),
                                createdAt,
                                updatedAt);
        }

}
