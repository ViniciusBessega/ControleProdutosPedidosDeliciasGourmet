package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.Categoria;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.repository.ProdutoRepository;
import com.gerenciador.sistema_loja.repository.ProdutoSimplesRepository;
import com.gerenciador.sistema_loja.repository.TortaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private ProdutoRepository repository;
    private ProdutoSimplesRepository produtoSimplesRepository;
    private TortaRepository tortaRepository;

    public ProdutoService(ProdutoSimplesRepository simplesRepository,
                          TortaRepository tortaRepository, ProdutoRepository repository) {
        this.produtoSimplesRepository = simplesRepository;
        this.tortaRepository = tortaRepository;
        this.repository = repository;
    }

//lista todos
    public List<Produto> listar(){
        return repository.findAll();
    }

    public Produto salvar(Produto produto){
        return repository.save(produto);
    }

    public void deletar(Long id){
        repository.deleteById(id);
    }

    public List<Produto> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    // lista doces
    public List<ProdutoSimples> listarDoces() {
        return produtoSimplesRepository.findByCategoria(Categoria.DOCE);
    }

    //lista salgados
    public List<ProdutoSimples> listarSalgados() {
        return produtoSimplesRepository.findByCategoria(Categoria.SALGADO);
    }

    //lista tortas
    public List<Torta> listarTortas() {
        return tortaRepository.findAll();
    }

}
