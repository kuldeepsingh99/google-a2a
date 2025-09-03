package com.kuldeep.tech.seller.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerAgentTools {

    private final List<Seller> sellerList =
            List.of( new Seller("1", "Mike", "Paris", 7),
                    new Seller("2", "Harry", "Lyon", 9),
                    new Seller("3", "John", "Berlin", 5),
                    new Seller("4", "Daniel", "London", 4));


    @Tool(name = "Get Seller By Id", description = "Return seller by Id")
    public Seller getSellerById(
            @ToolParam(description = "Seller Id") String sellerId
    ) {
        return sellerList.stream()
                .filter(o -> o.sellerId.equals(sellerId))
                .findFirst()
                .orElse(null);
    }

    @Tool(name = "Get Seller By Name", description = "Return seller by name")
    public Seller getSellerByName(
            @ToolParam(description = "Seller Name") String sellerName
    ) {
        return sellerList.stream()
                .filter(o -> o.name.equals(sellerName))
                .findFirst()
                .orElse(null);
    }

    public record Seller(String sellerId, String name, String address, int rating) {}
}
