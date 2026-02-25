package com.panda.salon_mgt_backend.configs.billing;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RazorpayBillingProvider implements BillingProvider {
    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Override
    public BillingProviderType name() {
        return BillingProviderType.RAZORPAY;
    }

    private String buildFrontendCheckoutUrl(String orderId) {
        return "http://localhost:5173/pay/razorpay?orderId=" + orderId;
    }

    @Override
    public CheckoutSession createCheckout(Salon salon, Plan plan, BillingTransaction tx) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", plan.getPriceMonthly()); // paise
            options.put("currency", "INR");
            options.put("receipt", "salon_" + salon.getSalonId());
            options.put("payment_capture", 1);

            Order order = client.orders.create(options);

            String orderId = order.get("id");

            return new CheckoutSession(
                    buildFrontendCheckoutUrl(orderId),
                    orderId
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    @Override
    public BillingResult verifyPayment(String payload, String signature) {
        try {
            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            if (!"order.paid".equals(eventType)) {
                return new BillingResult(null, null, false);
            }

            JSONObject payment = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = payment.getString("order_id");
            String paymentId = payment.getString("id");

            return new BillingResult(orderId, paymentId, true);

        } catch (Exception e) {
            throw new RuntimeException("Invalid Razorpay webhook", e);
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        try {
            Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}