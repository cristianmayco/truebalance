package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.Category;

import java.time.LocalDateTime;

public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponseDTO() {
    }

    public CategoryResponseDTO(Long id, String name, String description, String color,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CategoryResponseDTO fromCategory(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getColor(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
