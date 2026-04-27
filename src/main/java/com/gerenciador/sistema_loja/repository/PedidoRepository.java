package com.gerenciador.sistema_loja.repository;

import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByNomeClienteContainingIgnoreCase(String nomeCliente);

    List<Pedido> findAllByOrderByDataDesc();

    List<Pedido> findAllByOrderByDataAsc();

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.id = :id")
    java.util.Optional<Pedido> findByIdComItens(@Param("id") Long id);
}
