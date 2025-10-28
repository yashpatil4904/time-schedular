package com.meetingscheduler.repository;

import com.meetingscheduler.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, UUID> {
    
    List<Participation> findByUserId(UUID userId);
    
    List<Participation> findByMeetingId(UUID meetingId);
    
    List<Participation> findByRole(Participation.ParticipationRole role);
    
    List<Participation> findByResponseStatus(Participation.ResponseStatus responseStatus);
    
    @Query("SELECT p FROM Participation p WHERE p.user.id = :userId AND p.responseStatus = :status")
    List<Participation> findByUserIdAndResponseStatus(@Param("userId") UUID userId, 
                                                     @Param("status") Participation.ResponseStatus status);
}
