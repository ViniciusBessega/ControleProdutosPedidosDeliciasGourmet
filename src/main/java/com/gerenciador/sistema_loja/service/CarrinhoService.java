package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.Produto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CarrinhoService {

    // 🔥 chave = ID do produto
    private Map<Long, Integer> itens = new HashMap<>();

    // 🔥 guarda o produto real
    private Map<Long, Produto> produtos = new HashMap<>();

    public void adicionar(Produto p) {
        Long id = p.getId();

        itens.put(id, itens.getOrDefault(id, 0) + 1);
        produtos.put(id, p);
    }

    public void remover(Produto p) {
        Long id = p.getId();

        if (itens.containsKey(id)) {
            int q = itens.get(id) - 1;

            if (q <= 0) {
                itens.remove(id);
                produtos.remove(id);
            } else {
                itens.put(id, q);
            }
        }
    }

    public Map<Long, Integer> getItens() {
        return itens;
    }

    public Produto getProduto(Long id) {
        return produtos.get(id);
    }

    public void limpar() {
        itens.clear();
        produtos.clear();
    }
}