package com.kuldeep.tech.order.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderAgentTools {

    private final List<Order> orders =
            List.of( new Order("1", "B2B", "1", 100.50, "James"),
                    new Order("2", "B2B", "2", 50.75,"William"),
                    new Order("3", "B2C", "3", 130.00,"Oliver"),
                    new Order("4", "B2C", "4", 330.00,"Ethan"));

    @Tool(name = "Get B2B Orders", description = "Returns all B2B orders")
    public List<Order> getB2BOrders() {
        return orders.stream()
                .filter(o -> o.type().equalsIgnoreCase("B2B")).toList();

    }

    @Tool(name = "Get B2C Orders", description = "Returns all B2C orders")
    public List<Order> getB2COrders() {
        return orders.stream()
                .filter(o -> o.type().equalsIgnoreCase("B2C")).toList();
    }

    public record Order(String id, String type, String sellerId, double amount, String customerName) {}
}
