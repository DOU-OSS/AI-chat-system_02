package com.example.aichatsystem.config;

import lombok.Data;
import java.util.List;

@Data
public class ModelConfig {
    private List<Model> models;
    private String defaultModel;
    
    @Data
    public static class Model {
        private String id;
        private String name;
        private String provider;
        private String icon;
        private String url;
        private String apiKey;
        private boolean enabled;
        private String description;
    }
}
