package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dic_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DicStatus {

    @Id
    private Long id;

    @Column(name = "name_en") private String nameEn;
    @Column(name = "name_kz") private String nameKz;
    @Column(name = "name_ru") private String nameRu;
}
