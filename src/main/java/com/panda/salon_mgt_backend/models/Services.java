package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Services")
@Table(
        name = "services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_service_name_per_salon",
                        columnNames = {"salon_id", "service_name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Column(name = "service_price", nullable = false)
    private BigDecimal servicePrice;

    @NotNull
    @Min(5)
    @Max(600)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "salon_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_salon")
    )
    private Salon salon;

    @ManyToMany(mappedBy = "services")
    private Set<User> staff = new HashSet<>();

}
