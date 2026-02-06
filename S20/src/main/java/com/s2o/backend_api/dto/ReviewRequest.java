// package com.s2o.backend_api.dto;
// import lombok.Data;

// @Data
// public class ReviewRequest {
//     private Long userId;
//     private Long menuItemId;
//     private Integer rating;
//     private String comment;
// }
package com.s2o.backend_api.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long itemId; // ID món ăn
    private Integer rating;
    private String comment;
}