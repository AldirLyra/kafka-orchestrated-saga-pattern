package br.com.microservices.orchestrated.productvalidationservice.core.dto;

import java.time.LocalDateTime;

import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class History {
    
    private String source;
    
    private ESagaStatus status;
    
    private String message;
    
    private LocalDateTime createdAt;
    
}
