package com.gerenciador.sistema_loja.repository;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByCategoria(Categoria categoria);
}
