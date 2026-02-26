package com.panda.salon_mgt_backend.configs.billing;

import com.panda.salon_mgt_backend.models.PlanType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@Getter
public class StripePriceConfig {

    private final Map<PlanType, String> priceMap = new EnumMap<>(PlanType.class);

    public StripePriceConfig(
            @Value("${stripe.prices.pro}") String proPrice,
            @Value("${stripe.prices.premium}") String premiumPrice
    ) {
        priceMap.put(PlanType.PRO, proPrice);
        priceMap.put(PlanType.PREMIUM, premiumPrice);
    }

    public String getPriceId(PlanType type) {
        String price = priceMap.get(type);
        if (price == null) {
            throw new IllegalStateException("No Stripe price configured for plan: " + type);
        }
        return price;
    }
}