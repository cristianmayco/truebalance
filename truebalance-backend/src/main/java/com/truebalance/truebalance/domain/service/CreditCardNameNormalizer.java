package com.truebalance.truebalance.domain.service;

public class CreditCardNameNormalizer {

    /**
     * Normaliza o nome do cartão de crédito para garantir consistência.
     * Remove espaços extras, converte para minúsculas e remove caracteres especiais.
     *
     * @param name Nome do cartão a ser normalizado
     * @return Nome normalizado
     */
    public static String normalize(String name) {
        if (name == null) {
            return name;
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        // Remove espaços extras, converte para minúsculas e remove caracteres especiais
        return trimmed
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }
}
