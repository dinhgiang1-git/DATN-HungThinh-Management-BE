package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.TableFee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository qu?n lý truy xu?t d? li?u cho TableFee.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface TableFeeRepository extends JpaRepository<TableFee, Long> {
}
