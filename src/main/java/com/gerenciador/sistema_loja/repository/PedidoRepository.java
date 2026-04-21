package com.gerenciador.sistema_loja.repository;

import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByNomeClienteContainingIgnoreCase(String nomeCliente);

    List<Pedido> findAllByOrderByDataDesc();

    List<Pedido> findAllByOrderByDataAsc();
}
