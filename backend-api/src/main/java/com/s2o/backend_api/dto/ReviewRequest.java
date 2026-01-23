package com.s2o.backend_api.dto;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long menuItemId;
    private Integer rating;
    private String comment;
}