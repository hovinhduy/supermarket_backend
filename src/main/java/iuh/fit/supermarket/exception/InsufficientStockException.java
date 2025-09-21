package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi không đủ tồn kho để thực hiện giao dịch
 */
public class InsufficientStockException extends WarehouseException {

    private final Long variantId;
    private final Integer requiredQuantity;
    private final Integer availableQuantity;

    /**
     * Constructor với thông tin chi tiết về tồn kho
     */
    public InsufficientStockException(Long variantId, Integer requiredQuantity, Integer availableQuantity) {
        super(String.format("Không đủ tồn kho cho biến thể ID %d. Yêu cầu: %d, Có sẵn: %d", 
                           variantId, requiredQuantity, availableQuantity));
        this.variantId = variantId;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Constructor với message tùy chỉnh
     */
    public InsufficientStockException(String message, Long variantId, Integer requiredQuantity, Integer availableQuantity) {
        super(message);
        this.variantId = variantId;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
