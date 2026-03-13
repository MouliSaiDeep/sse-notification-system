package com.gpp.sse_notification_system.repository;

import com.gpp.sse_notification_system.model.UserSubscription;
import com.gpp.sse_notification_system.model.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
    List<UserSubscription> findByUserId(Integer userId);
}
