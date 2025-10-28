package com.meetingscheduler.repository;

import com.meetingscheduler.entity.ManageMeetingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManageMeetingRequestRepository extends JpaRepository<ManageMeetingRequest, UUID> {
    
    List<ManageMeetingRequest> findByRequesterId(UUID requesterId);
    
    List<ManageMeetingRequest> findByExecutiveApproval(Boolean executiveApproval);
    
    @Query("SELECT m FROM ManageMeetingRequest m WHERE m.requesterId = :requesterId AND m.executiveApproval = false")
    List<ManageMeetingRequest> findPendingRequestsByRequesterId(@Param("requesterId") UUID requesterId);
}
