package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dic_is")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DicIs {

    @Id
    private Long id;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_kz")
    private String nameKz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "include_in_availability", nullable = false)
    private Boolean includeInAvailability = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "go_id", nullable = false)
    private DicGo go;
}
