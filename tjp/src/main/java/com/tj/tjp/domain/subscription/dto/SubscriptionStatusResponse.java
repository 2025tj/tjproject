package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName= "of")
public class SubscriptionStatusResponse {
    private String status;
    private SubscriptionResponse details;

    public static SubscriptionStatusResponse none() {
        return new SubscriptionStatusResponse("NONE", null);
    }

    public static SubscriptionStatusResponse from (Subscription subscription) {
        if (subscription == null) return none();
        String status = subscription.isActive() ? "ACTIVE" : subscription.getStatus().name();
        return new SubscriptionStatusResponse(status, SubscriptionResponse.from(subscription));
    }
}
