package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.ItemPedido;
import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.repository.ItemPedidoRepository;
import com.gerenciador.sistema_loja.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private PedidoRepository pedidoRepository;
    private ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }


    public Pedido salvar(Pedido pedido) {

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            item.setPedido(pedido);

            BigDecimal subtotal = item.getQuantidade()
                    .multiply(item.getPrecoUnitario());

            total = total.add(subtotal);
        }

        // 🔹 aplica desconto (se existir)
        if (pedido.getDesconto() != null) {

            BigDecimal percentual = pedido.getDesconto()
                    .divide(new BigDecimal("100"));

            BigDecimal valorDesconto = total.multiply(percentual);

            total = total.subtract(valorDesconto);
        }

        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    public void deletar(Long id){
        pedidoRepository.deleteById(id);
    }

    public List<Pedido> buscarPorNomeCliente(String nome) {
        return pedidoRepository.findByNomeClienteContainingIgnoreCase(nome);
    }

    public Pedido alterarPrecoItem(Long id, BigDecimal novoPreco) {

        ItemPedido item = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        item.setPrecoUnitario(novoPreco);

        Pedido pedido = item.getPedido();

        recalcularTotal(pedido);

        return pedidoRepository.save(pedido);
    }

    private void recalcularTotal(Pedido pedido) {

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            BigDecimal subtotal = item.getQuantidade()
                    .multiply(item.getPrecoUnitario());

            total = total.add(subtotal);
        }

        if (pedido.getDesconto() != null &&
                pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal percentual = pedido.getDesconto().divide(new BigDecimal("100"));

            BigDecimal valorDesconto = total.multiply(percentual);

            total = total.subtract(valorDesconto);
        }

        pedido.setTotal(total);
    }



    public Pedido alterarTotal(Long pedidoId, BigDecimal novoTotal) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setTotal(novoTotal);

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarOrdenado(boolean maisRecentes) {
        if (maisRecentes) {
            return pedidoRepository.findAllByOrderByDataDesc();
        } else {
            return pedidoRepository.findAllByOrderByDataAsc();
        }
    }

    public Page<Pedido> listarPaginado(int pagina, int tamanho, boolean maisRecentes) {

        Sort sort;

        if (maisRecentes) {
            sort = Sort.by("data").descending();
        } else {
            sort = Sort.by("data").ascending();
        }

        PageRequest pageRequest = PageRequest.of(pagina, tamanho, sort);

        return pedidoRepository.findAll(pageRequest);
    }
}
