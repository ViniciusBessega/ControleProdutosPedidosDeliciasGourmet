package com.gerenciador.sistema_loja.repository;

import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TortaRepository extends JpaRepository<Torta, Long> {
}
