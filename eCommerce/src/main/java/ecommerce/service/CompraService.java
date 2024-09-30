package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
	
		if (carrinho == null) {
			return new CompraDTO(false, null, "Carrinho não encontrado para o cliente."); // Retornar DTO com erro
		}
	
		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());
	
		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);
	
		if (!disponibilidade.disponivel()) {
			return new CompraDTO(false, null, "Itens fora de estoque."); // Retornar DTO com erro
		}
	
		BigDecimal custoTotal = calcularCustoTotal(carrinho);
	
		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());
	
		if (!pagamento.autorizado()) {
			// Chamar cancelamento do pagamento simulado
			pagamentoExternal.cancelarPagamento(cliente.getId(), null); // Você pode passar o ID da transação se necessário
			return new CompraDTO(false, null, "Pagamento não autorizado."); // Retornar DTO com erro
		}
	
		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);
	
		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			return new CompraDTO(false, null, "Erro ao dar baixa no estoque."); // Retornar DTO com erro
		}
	
		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");
	
		return compraDTO;
	}
	
	

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		BigDecimal custoTotalProdutos = carrinho.getItens().stream()
				.map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	
		// Calcular o frete
		BigDecimal frete = calcularFrete(carrinho);
	
		// Aplicar descontos ao custo total dos produtos
		custoTotalProdutos = aplicarDescontos(custoTotalProdutos, carrinho);
	
		// Adicionar o frete ao custo total, exceto para cliente do tipo OURO
		Cliente cliente = carrinho.getCliente();
		if (cliente != null && cliente.getTipo() == TipoCliente.OURO) {
			return custoTotalProdutos; // Sem frete para cliente OURO
		} else if (cliente != null && cliente.getTipo() == TipoCliente.PRATA) {
			// Aplica 50% de desconto no frete
			return custoTotalProdutos.add(frete.multiply(BigDecimal.valueOf(0.5)));
		} else {
			// Para Bronze e outros clientes, o frete é integral
			return custoTotalProdutos;
		}
	}
	

	// Método para calcular o frete
	private BigDecimal calcularFrete(CarrinhoDeCompras carrinho) {
		// Calcular o peso total dos itens
		double pesoTotal = carrinho.getItens().stream()
				.mapToDouble(item -> item.getProduto().getPeso() * item.getQuantidade())
				.sum();

		BigDecimal frete = BigDecimal.ZERO;

		// Calcular o frete com base no peso total
		if (pesoTotal > 50) {
			frete = BigDecimal.valueOf(7.00).multiply(BigDecimal.valueOf(pesoTotal));
		} else if (pesoTotal >= 10) {
			frete = BigDecimal.valueOf(4.00).multiply(BigDecimal.valueOf(pesoTotal));
		} else if (pesoTotal >= 5) {
			frete = BigDecimal.valueOf(2.00).multiply(BigDecimal.valueOf(pesoTotal));
		}
		System.out.println("Custo Total FRETE: " + frete);
		return frete;
	}

	private BigDecimal aplicarDescontos(BigDecimal custoTotal, CarrinhoDeCompras carrinho) {
		// Desconto de acordo com o valor total da compra
		if (custoTotal.compareTo(BigDecimal.valueOf(1000.00)) > 0) {
			custoTotal = custoTotal.multiply(BigDecimal.valueOf(0.80)); // 20% de desconto
		} else if (custoTotal.compareTo(BigDecimal.valueOf(500.00)) > 0) {
			custoTotal = custoTotal.multiply(BigDecimal.valueOf(0.90)); // 10% de desconto
		}
		return custoTotal; // Retornar custo total após aplicar descontos
	}
	
	

}
