package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "my_service_fields")
@Getter @Setter @NoArgsConstructor
public class MyServiceField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "direction", nullable = false)
    private String direction; // REQUEST or RESPONSE

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "field_number")
    private String fieldNumber;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "tag_name")
    private String tagName;

    @Column(name = "format_info")
    private String formatInfo;

    @Column(name = "size_info")
    private String sizeInfo;

    @Column(name = "is_required")
    private String isRequired;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "format_id")
    private Long formatId;
}
