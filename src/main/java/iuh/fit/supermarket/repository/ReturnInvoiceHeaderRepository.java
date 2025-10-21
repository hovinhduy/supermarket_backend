package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ReturnInvoiceHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ReturnInvoiceHeader
 */
@Repository
public interface ReturnInvoiceHeaderRepository extends JpaRepository<ReturnInvoiceHeader, Integer> {

    /**
     * Tìm tất cả phiếu trả theo hóa đơn gốc
     */
    List<ReturnInvoiceHeader> findByOriginalInvoice_InvoiceId(Integer invoiceId);

    /**
     * Tìm phiếu trả theo mã phiếu trả
     */
    Optional<ReturnInvoiceHeader> findByReturnCode(String returnCode);
}
