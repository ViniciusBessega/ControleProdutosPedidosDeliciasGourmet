package com.gerenciador.sistema_loja.model.tiposproduto;

import com.gerenciador.sistema_loja.model.Produto;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSimples extends Produto {

    private BigDecimal preco;


}
