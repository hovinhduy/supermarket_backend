package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu thuộc tính
 */
@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    /**
     * Tìm thuộc tính theo tên
     */
    Optional<Attribute> findByName(String name);

    /**
     * Kiểm tra sự tồn tại của tên thuộc tính
     */
    boolean existsByName(String name);
}
