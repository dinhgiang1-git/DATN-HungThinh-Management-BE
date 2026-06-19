package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Feedback;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Feedback.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {
    long countByFeedbackStatus(FeedbackStatus status);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.response IS NULL OR f.response = ''")
    long countUnansweredFeedbacks();

    @Query("""
            select f
            from Feedback f
            left join fetch f.apartment
            left join fetch f.sender
            left join fetch f.device
            where f.feedbackStatus in :statuses
            order by f.createdAt desc, f.id desc
            """)
    List<Feedback> findOpenIssuesForAdminChat(@Param("statuses") Collection<FeedbackStatus> statuses, Pageable pageable);

    @Query("""
            select f
            from Feedback f
            left join fetch f.apartment
            left join fetch f.sender
            left join fetch f.device
            where f.apartment.id = :apartmentId
            order by f.createdAt desc, f.id desc
            """)
    List<Feedback> findByApartmentForAdminChat(@Param("apartmentId") Long apartmentId, Pageable pageable);
}
