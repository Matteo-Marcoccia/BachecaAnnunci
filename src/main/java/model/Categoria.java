package model;

public record Categoria(
        int codiceCategoria,
        String nome,
        Integer categoriaPadre,
        String gestoreCreatore
) {
}
