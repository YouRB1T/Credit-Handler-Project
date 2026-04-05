    package com.credithandler.deal.model;

    import com.credithandler.api.dto.calc.PaymentScheduleElementDto;
    import com.credithandler.deal.model.enums.CreditStatus;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import lombok.AllArgsConstructor;
    import lombok.ToString;
    import org.hibernate.annotations.JdbcTypeCode;
    import org.hibernate.type.SqlTypes;

    import java.math.BigDecimal;
    import java.util.List;
    import java.util.UUID;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    public class Credit {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID creditId;

        private BigDecimal amount;
        private Integer term;
        private BigDecimal monthlyPayment;
        private BigDecimal rate;
        private BigDecimal psk;
        @JdbcTypeCode(SqlTypes.JSON)
        private List<PaymentScheduleElementDto> paymentSchedule;
        private Boolean insuranceEnabled;
        private boolean salaryClient;
        @Enumerated(EnumType.STRING)
        private CreditStatus creditStatus;
    }
