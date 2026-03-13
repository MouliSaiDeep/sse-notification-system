package com.gpp.sse_notification_system.repository;

import com.gpp.sse_notification_system.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByChannelInAndIdGreaterThanOrderByIdAsc(List<String> channels, Long id);
    
    Page<Event> findByChannelAndIdGreaterThanOrderByIdAsc(String channel, Long id, Pageable pageable);
    
    Page<Event> findByChannelOrderByIdAsc(String channel, Pageable pageable);
}
