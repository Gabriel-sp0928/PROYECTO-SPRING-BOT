package com.industriagafra.dto;

import com.industriagafra.entity.QuoteStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuoteDTO {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime date;
    private QuoteStatus status;
    private BigDecimal total;
    private BigDecimal discount;
    private BigDecimal tax;
    private List<QuoteDetailDTO> details;
}