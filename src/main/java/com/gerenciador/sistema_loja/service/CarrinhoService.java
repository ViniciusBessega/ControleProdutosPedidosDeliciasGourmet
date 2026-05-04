package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.Produto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarrinhoService {

    private Map<Long, BigDecimal> itens = new HashMap<>();
    private Map<Long, Produto> produtos = new HashMap<>();

    private String nomeCliente = "";
    private LocalDate dataEntrega = null;

    public void adicionar(Produto p) {
        Long id = p.getId();
        itens.put(id, itens.getOrDefault(id, BigDecimal.ZERO).add(BigDecimal.ONE));
        produtos.put(id, p);
    }

    public void remover(Produto p) {
        Long id = p.getId();
        if (itens.containsKey(id)) {
            BigDecimal q = itens.get(id).subtract(BigDecimal.ONE);
            if (q.compareTo(BigDecimal.ZERO) <= 0) {
                itens.remove(id);
                produtos.remove(id);
            } else {
                itens.put(id, q);
            }
        }
    }

    public Map<Long, BigDecimal> getItens() { return itens; }

    public Map<Long, Produto> getProdutos() { return produtos; }

    public Produto getProduto(Long id) { return produtos.get(id); }

    public void limpar() {
        itens.clear();
        produtos.clear();
        nomeCliente = "";
        dataEntrega = null;
    }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nome) { this.nomeCliente = nome; }

    public LocalDate getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDate data) { this.dataEntrega = data; }
}