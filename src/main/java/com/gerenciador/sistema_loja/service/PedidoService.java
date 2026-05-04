package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.ItemPedido;
import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.repository.ItemPedidoRepository;
import com.gerenciador.sistema_loja.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public Pedido salvar(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            item.setPedido(pedido);
            total = total.add(item.getQuantidade().multiply(item.getPrecoUnitario()));
        }

        if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorDesconto = total.multiply(
                    pedido.getDesconto().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
            total = total.subtract(valorDesconto);
        }

        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return pedidoRepository.save(pedido);
    }

    public void deletar(Long id) {
        pedidoRepository.deleteById(id);
    }

    public List<Pedido> buscarPorNomeCliente(String nome) {
        return pedidoRepository.findByNomeClienteContainingIgnoreCase(nome);
    }

    public Pedido alterarPrecoItem(Long id, BigDecimal novoPreco) {
        ItemPedido item = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        item.setPrecoUnitario(novoPreco);
        recalcularTotal(item.getPedido());
        return pedidoRepository.save(item.getPedido());
    }

    private void recalcularTotal(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            total = total.add(item.getQuantidade().multiply(item.getPrecoUnitario()));
        }

        if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorDesconto = total.multiply(
                    pedido.getDesconto().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
            total = total.subtract(valorDesconto);
        }

        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    public Pedido alterarTotal(Long pedidoId, BigDecimal novoTotal) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setTotal(novoTotal);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarOrdenado(boolean maisRecentes) {
        return maisRecentes
                ? pedidoRepository.findAllByOrderByDataDesc()
                : pedidoRepository.findAllByOrderByDataAsc();
    }

    public Page<Pedido> listarPaginado(int pagina, int tamanho, boolean maisRecentes) {
        Sort sort = maisRecentes
                ? Sort.by("data").descending()
                : Sort.by("data").ascending();
        return pedidoRepository.findAll(PageRequest.of(pagina, tamanho, sort));
    }

    public Pedido salvarSemRecalcular(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public Pedido buscarComItens(Long id) {
        return pedidoRepository.findByIdComItens(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}