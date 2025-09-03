package com.kuldeep.tech.parent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AgentProperties {

    private List<AgentConfig> agents;
    @Data
    public static class AgentConfig {
        private String id;
        private String name;
        private String url;
        private List<String> skills;
    }
}
