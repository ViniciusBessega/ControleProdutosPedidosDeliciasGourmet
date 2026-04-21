package com.gerenciador.sistema_loja.repository;

import com.gerenciador.sistema_loja.model.tiposproduto.Categoria;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoSimplesRepository extends JpaRepository<ProdutoSimples, Long> {

    List<ProdutoSimples> findByCategoria(Categoria categoria);
}
