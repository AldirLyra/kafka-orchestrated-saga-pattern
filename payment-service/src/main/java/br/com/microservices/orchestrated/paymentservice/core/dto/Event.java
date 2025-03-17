package br.com.microservices.orchestrated.paymentservice.core.dto;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    
    private String id;
    
    private String transactionId;
    
    private String orderId;
    
    private Order payload;
    
    private String source;
    
    private ESagaStatus status;
    
    private List<History> eventHistory;
    
    private LocalDateTime createdAt;
    
    public void addHistory(final History history) {
        if (isEmpty(this.eventHistory)) {
            this.eventHistory = new ArrayList<>();
        }
        
        this.eventHistory.add(history);
    }
    
}
