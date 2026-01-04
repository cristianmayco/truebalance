package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequestDTO {

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @Size(max = 7, message = "Cor deve estar no formato hexadecimal (#RRGGBB)")
    private String color;

    public CategoryRequestDTO() {
    }

    public CategoryRequestDTO(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
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
}
