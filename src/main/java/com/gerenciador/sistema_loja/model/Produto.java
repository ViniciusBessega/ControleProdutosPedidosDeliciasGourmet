package com.gerenciador.sistema_loja.model;

import com.gerenciador.sistema_loja.model.tiposproduto.Categoria;
import jakarta.persistence.*;
        import lombok.*;
        import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @OneToMany(mappedBy = "produto")
    private List<ItemPedido> itens;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;
}
