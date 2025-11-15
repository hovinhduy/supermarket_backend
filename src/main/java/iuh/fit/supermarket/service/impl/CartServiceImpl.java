package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.structured.CartInfo;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.PriceType;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của CartService
 * Quản lý giỏ hàng cho chatbot
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductUnitRepository productUnitRepository;
    private final PriceDetailRepository priceDetailRepository;
    private final CustomerRepository customerRepository;

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Override
    @Transactional
    public CartInfo addToCart(Integer customerId, Long productUnitId, Integer quantity) {
        log.info("Thêm sản phẩm vào giỏ hàng - Customer: {}, ProductUnit: {}, Quantity: {}", 
                customerId, productUnitId, quantity);

        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Kiểm tra customer tồn tại
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + customerId));

        // Kiểm tra product unit tồn tại
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productUnitId));

        // Lấy giá hiện tại
        PriceDetail priceDetail = priceDetailRepository
                .findCurrentPriceByProductUnitId(productUnitId, PriceType.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có giá bán"));

        // Tìm hoặc tạo giỏ hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setCustomer(customer);
                    return shoppingCartRepository.save(newCart);
                });

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductUnitId(cart.getCartId(), productUnitId)
                .orElse(null);

        if (cartItem != null) {
            // Cập nhật số lượng
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
            log.debug("Cập nhật số lượng sản phẩm trong giỏ: {}", cartItem.getQuantity());
        } else {
            // Thêm mới
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductUnit(productUnit);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(priceDetail.getSalePrice().doubleValue());
            cartItemRepository.save(cartItem);
            log.debug("Thêm sản phẩm mới vào giỏ");
        }

        return getCart(customerId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Override
    @Transactional
    public CartInfo removeFromCart(Integer customerId, Long productUnitId) {
        log.info("Xóa sản phẩm khỏi giỏ hàng - Customer: {}, ProductUnit: {}", 
                customerId, productUnitId);

        // Tìm giỏ hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        // Xóa cart item
        cartItemRepository.deleteByCartIdAndProductUnitId(cart.getCartId(), productUnitId);
        log.debug("Đã xóa sản phẩm khỏi giỏ hàng");

        return getCart(customerId);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Override
    @Transactional
    public CartInfo updateQuantity(Integer customerId, Long productUnitId, Integer quantity) {
        log.info("Cập nhật số lượng sản phẩm - Customer: {}, ProductUnit: {}, NewQuantity: {}", 
                customerId, productUnitId, quantity);

        // Validate quantity
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        // Nếu quantity = 0, xóa sản phẩm
        if (quantity == 0) {
            return removeFromCart(customerId, productUnitId);
        }

        // Tìm giỏ hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        // Tìm cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductUnitId(cart.getCartId(), productUnitId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        // Cập nhật số lượng
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        log.debug("Đã cập nhật số lượng sản phẩm: {}", quantity);

        return getCart(customerId);
    }

    /**
     * Lấy thông tin giỏ hàng
     */
    @Override
    @Transactional(readOnly = true)
    public CartInfo getCart(Integer customerId) {
        log.info("Lấy thông tin giỏ hàng - Customer: {}", customerId);

        // Tìm giỏ hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElse(null);

        // Nếu chưa có giỏ, trả về giỏ rỗng
        if (cart == null) {
            log.debug("Khách hàng chưa có giỏ hàng");
            return new CartInfo(
                    null,
                    List.of(),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    LocalDateTime.now()
            );
        }

        // Lấy danh sách cart items
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        log.debug("Tìm thấy {} items trong giỏ hàng", cartItems.size());

        // Map sang CartInfo
        List<CartInfo.CartItemInfo> itemInfos = new ArrayList<>();
        double subTotal = 0.0;

        for (CartItem item : cartItems) {
            ProductUnit productUnit = item.getProductUnit();
            Product product = productUnit.getProduct();
            
            double originalTotal = item.getUnitPrice() * item.getQuantity();
            double finalTotal = originalTotal; // Tạm thời không tính khuyến mãi

            // Lấy URL hình ảnh primary của ProductUnit (nếu có)
            String imageUrl = null;
            if (productUnit.getProductUnitImages() != null && !productUnit.getProductUnitImages().isEmpty()) {
                // Tìm hình ảnh primary
                for (ProductUnitImage unitImage : productUnit.getProductUnitImages()) {
                    if (unitImage.getIsPrimary() != null && unitImage.getIsPrimary()) {
                        imageUrl = unitImage.getProductImage().getImageUrl();
                        break;
                    }
                }
                // Nếu không có primary, lấy hình đầu tiên
                if (imageUrl == null) {
                    imageUrl = productUnit.getProductUnitImages().get(0).getProductImage().getImageUrl();
                }
            }

            CartInfo.CartItemInfo itemInfo = new CartInfo.CartItemInfo(
                    productUnit.getId(),
                    product.getName(),
                    productUnit.getUnit().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    originalTotal,
                    finalTotal,
                    imageUrl,
                    null, // stock quantity - cần stock service
                    false, // has promotion
                    null  // promotion name
            );

            itemInfos.add(itemInfo);
            subTotal += originalTotal;
        }

        // Tính tổng
        double lineItemDiscount = 0.0; // Tạm thời không tính
        double orderDiscount = 0.0;    // Tạm thời không tính
        double totalPayable = subTotal - lineItemDiscount - orderDiscount;

        return new CartInfo(
                cart.getCartId(),
                itemInfos,
                cartItems.size(),
                subTotal,
                lineItemDiscount,
                orderDiscount,
                totalPayable,
                cart.getUpdatedAt()
        );
    }

    /**
     * Xóa tất cả sản phẩm trong giỏ hàng
     */
    @Override
    @Transactional
    public CartInfo clearCart(Integer customerId) {
        log.info("Xóa tất cả sản phẩm trong giỏ hàng - Customer: {}", customerId);

        // Tìm giỏ hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElse(null);

        if (cart != null) {
            // Xóa tất cả items
            cartItemRepository.deleteByCartId(cart.getCartId());
            log.debug("Đã xóa tất cả items trong giỏ hàng");
        }

        return getCart(customerId);
    }
}
