package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "salons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salon_id")
    private Long salonId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "salon_name", nullable = false)
    private String salonName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "salon_address", nullable = false)
    private String salonAddress;

    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;
}

