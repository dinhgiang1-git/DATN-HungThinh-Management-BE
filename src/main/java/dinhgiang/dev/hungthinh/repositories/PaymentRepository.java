package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Payment;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentMethod;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Payment.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
            select count(p)
            from Payment p
            where p.invoice.id = :invoiceId
              and p.paymentMethod = :paymentMethod
              and p.paymentStatus = :paymentStatus
            """)
    long countPendingPayment(
            @Param("invoiceId") Long invoiceId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    Optional<Payment> findByPaymentMethodAndTxnRef(PaymentMethod paymentMethod, String txnRef);

    Optional<Payment> findByPaymentMethodAndTransactionNo(PaymentMethod paymentMethod, String transactionNo);

    @Query("""
            select p
            from Payment p
            join fetch p.invoice i
            where i.id in :invoiceIds
            order by p.paymentDateTime desc, p.id desc
            """)
    List<Payment> findByInvoiceIdsForAdminChat(@Param("invoiceIds") Collection<Long> invoiceIds);
}
