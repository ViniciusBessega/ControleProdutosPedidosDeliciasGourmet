package com.gerenciador.sistema_loja.model.tiposproduto;

import com.gerenciador.sistema_loja.model.Produto;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("TORTA")
public class Torta extends Produto {

    private BigDecimal precoPorKg;
    private String recheio;

}
