package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestExecutionListeners;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.external.fake.EstoqueSimulado;
import ecommerce.external.fake.PagamentoSimulado;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.service.CompraService;

public class CompraServiceTest {

        @InjectMocks
        private CompraService compraService;

        @Mock
        private CarrinhoDeCompras carrinho;

        @Mock
        private Cliente cliente;

        @Mock
        private CarrinhoDeComprasService carrinhoService;

        @Mock
        private ClienteService clienteService;

        @Mock
        private IPagamentoExternal pagamentoExternal;

        @Mock
        private CarrinhoDeComprasRepository carrinhoRepository;

        @Mock
        private IEstoqueExternal estoqueExternal;

        private EstoqueSimulado estoqueSimulado;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                estoqueSimulado = new EstoqueSimulado();
        }

        @Test
        void verificarDisponibilidade_ProdutosIndisponiveis() {
                // Dado uma lista de IDs de produtos com um produto
                Long produtoIndisponivelId = 1L;
                List<Long> produtosIds = Arrays.asList(produtoIndisponivelId);
                List<Long> produtosQuantidades = Arrays.asList(5L); // quantidade como Long

                // Quando verificar a disponibilidade
                DisponibilidadeDTO disponibilidade = estoqueSimulado.verificarDisponibilidade(produtosIds,
                                produtosQuantidades);

                // Então deve retornar que o produto está indisponível
                assertEquals(false, disponibilidade.disponivel());
                assertEquals(1, disponibilidade.idsProdutosIndisponiveis().size());
                assertEquals(produtoIndisponivelId, disponibilidade.idsProdutosIndisponiveis().get(0));
        }

        @Test
        void verificarDisponibilidade_ProdutosDisponiveis() {
                // Dado uma lista vazia de IDs de produtos
                List<Long> produtosIds = Arrays.asList(); // lista vazia
                List<Long> produtosQuantidades = Arrays.asList(); // lista vazia

                // Quando verificar a disponibilidade
                DisponibilidadeDTO disponibilidade = estoqueSimulado.verificarDisponibilidade(produtosIds,
                                produtosQuantidades);

                // Então deve retornar que não há produtos indisponíveis
                assertEquals(true, disponibilidade.disponivel());
                assertEquals(0, disponibilidade.idsProdutosIndisponiveis().size());
        }

        @Test
        public void testFinalizarCompra_Sucesso() {
                // Arrange: Preparar os dados de teste
                Long carrinhoId = 1L;
                Long clienteId = 1L;

                // Criação do produto e item
                Produto produto = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("200.00"), 1,
                                null);
                ItemCompra item = new ItemCompra(1L, produto, 1L);

                // Configurando o carrinho
                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setId(carrinhoId);
                carrinho.setCliente(new Cliente(clienteId, "Cliente Teste", "Endereço Teste", TipoCliente.PRATA));
                carrinho.setItens(Collections.singletonList(item));

                // Configurando mocks
                when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, carrinho.getCliente()))
                                .thenReturn(carrinho);
                when(clienteService.buscarPorId(clienteId)).thenReturn(carrinho.getCliente());

                // Mock para verificar a disponibilidade, retornando que está disponível
                when(estoqueExternal.verificarDisponibilidade(
                                Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

                // Mock para autorizar o pagamento
                when(pagamentoExternal.autorizarPagamento(clienteId, produto.getPreco().doubleValue()))
                                .thenReturn(new PagamentoDTO(true, 12345L)); // Simulando um pagamento autorizado

                // Mock para dar baixa no estoque
                when(estoqueExternal.darBaixa(Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new EstoqueBaixaDTO(true));

                // Act: Chamar o método a ser testado
                CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);

                // Assert: Verificar o resultado esperado
                assertNotNull(resultado);
                assertTrue(resultado.sucesso(), "A compra deve ser finalizada com sucesso.");
                assertEquals(Long.valueOf(12345), resultado.transacaoPagamentoId(),
                                "O ID da transação deve ser o esperado.");
                assertEquals("Compra finalizada com sucesso.", resultado.mensagem(), "A mensagem deve ser a esperada.");
        }

        @Test
        public void testFinalizarCompra_ItensIndisponiveis() {
                // Arrange: Preparar os dados de teste
                Long carrinhoId = 1L;
                Long clienteId = 1L;

                // Criação do produto e item
                Produto produto = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("200.00"), 1,
                                null);
                ItemCompra item = new ItemCompra(1L, produto, 1L);

                // Configurando o carrinho
                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setId(carrinhoId);
                carrinho.setCliente(new Cliente(clienteId, "Cliente Teste", "Endereço Teste", TipoCliente.PRATA));
                carrinho.setItens(Collections.singletonList(item));

                // Configurando mocks
                when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
                when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

                // Mock para verificar a disponibilidade, retornando que não está disponível
                when(estoqueExternal.verificarDisponibilidade(
                                Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new DisponibilidadeDTO(false, Collections.singletonList(produto.getId())));

                // Act: Chamar o método de finalizar compra
                CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);

                // Assert: Verificar o resultado
                assertFalse(resultado.sucesso(), "A compra deve ter falhado.");
                assertEquals("Itens fora de estoque.", resultado.mensagem(), "A mensagem de erro deve ser a esperada.");
        }

        @Test
        public void testFinalizarCompra_PagamentoNaoAutorizado() {
                // Arrange: Preparar os dados de teste
                Long carrinhoId = 1L;
                Long clienteId = 1L;

                // Criação do produto e item
                Produto produto = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("200.00"), 1,
                                null);
                ItemCompra item = new ItemCompra(1L, produto, 1L);

                // Configurando o carrinho
                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setId(carrinhoId);
                carrinho.setCliente(new Cliente(clienteId, "Cliente Teste", "Endereço Teste", TipoCliente.PRATA));
                carrinho.setItens(Collections.singletonList(item));

                // Configurando mocks
                when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, carrinho.getCliente()))
                                .thenReturn(carrinho);
                when(clienteService.buscarPorId(clienteId)).thenReturn(carrinho.getCliente());

                // Mock para verificar disponibilidade, retornando que está disponível
                when(estoqueExternal.verificarDisponibilidade(
                                Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

                // Mock para falha na autorização do pagamento usando PagamentoSimulado
                PagamentoSimulado pagamentoSimulado = new PagamentoSimulado();
                when(pagamentoExternal.autorizarPagamento(clienteId, produto.getPreco().doubleValue()))
                                .thenReturn(new PagamentoDTO(false, null)); // Simulando pagamento não autorizado

                // Act: Chamar o método a ser testado
                CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);

                // Assert: Verificar o resultado esperado
                assertNotNull(resultado);
                assertFalse(resultado.sucesso(),
                                "A compra não deve ser finalizada com sucesso quando o pagamento não é autorizado.");
                assertEquals("Pagamento não autorizado.", resultado.mensagem(),
                                "A mensagem deve indicar que o pagamento não foi autorizado.");

                // Cancelar pagamento simulado
                pagamentoSimulado.cancelarPagamento(clienteId, null); // Transação é nula pois não houve autorização
        }

        @Test
        public void testFinalizarCompra_ErroAoDarBaixaNoEstoque() {
                // Arrange: Preparar os dados de teste
                Long carrinhoId = 1L;
                Long clienteId = 1L;

                // Criação do produto e item
                Produto produto = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("200.00"), 1,
                                null);
                ItemCompra item = new ItemCompra(1L, produto, 1L);

                // Configurando o carrinho
                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setId(carrinhoId);
                carrinho.setCliente(new Cliente(clienteId, "Cliente Teste", "Endereço Teste", TipoCliente.PRATA));
                carrinho.setItens(Collections.singletonList(item));

                // Configurando mocks
                when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
                when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

                // Mock para verificar disponibilidade, retornando que está disponível
                when(estoqueExternal.verificarDisponibilidade(
                                Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

                // Mock para autorizar o pagamento com sucesso
                when(pagamentoExternal.autorizarPagamento(cliente.getId(), 200.00))
                                .thenReturn(new PagamentoDTO(true, 1L));

                // Mock para simular erro ao dar baixa no estoque
                when(estoqueExternal.darBaixa(
                                Collections.singletonList(produto.getId()),
                                Collections.singletonList(1L)))
                                .thenReturn(new EstoqueBaixaDTO(false));

                // Act: Chamar o método de finalizar compra
                CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);

                // Assert: Verificar o resultado
                assertFalse(resultado.sucesso(), "A compra deve ter falhado.");
                assertEquals("Erro ao dar baixa no estoque.", resultado.mensagem(),
                                "A mensagem de erro deve ser a esperada.");
        }

        @Test
        public void testCalcularCustoTotal_WithoutFrete_Ouro() {
                // Arrange: Configurar o carrinho com peso e produtos
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("400.00"), 2,
                                TipoProduto.ELETRONICO);
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição do Produto 2", new BigDecimal("200.00"), 2,
                                TipoProduto.ROUPA);

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.OURO)); // Cliente do tipo Ouro
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                // Assert: Verificar se o custo final está correto (sem frete para cliente Ouro)
                assertThat(custoTotal).isEqualTo(new BigDecimal("540.000"));
        }

        @Test
        public void testCalcularCustoTotal_WithFrete_Prata() {
                // Arrange: Configurar o carrinho com peso e produtos
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", new BigDecimal("500.00"), 4,
                                TipoProduto.ELETRONICO);
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", new BigDecimal("300.00"), 2,
                                TipoProduto.ROUPA);

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.PRATA));
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                // Assert: Verificar se o custo final está correto (com frete e desconto para
                // cliente Prata)
                assertEquals(new BigDecimal("726.000"), custoTotal);
        }

        @Test
        public void testCalcularCustoTotal_WithFrete_Bronze() {
                // Arrange: Configurar o carrinho com peso e produtos
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", new BigDecimal("800.00"), 7,
                                TipoProduto.ELETRONICO);
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", new BigDecimal("400.00"), 3,
                                TipoProduto.ROUPA);

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L); // Apenas um produto de cada

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.BRONZE));
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                assertEquals(new BigDecimal("960.000"), custoTotal);
        }

        @Test
        public void testCalcularCustoTotal_WithoutFrete_Ouro2() {
                // Arrange: Configurar o carrinho com peso e produtos
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("100.00"), 1,
                                TipoProduto.ELETRONICO);
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição do Produto 2", new BigDecimal("300.00"), 2,
                                TipoProduto.ROUPA);

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.OURO)); // Cliente do tipo Ouro
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                // Assert: Verificar se o custo final está correto (sem frete para cliente Ouro)
                assertThat(custoTotal).isEqualTo(new BigDecimal("400.00"));
        }

        @Test
        public void testCalcularCustoTotal_WithFrete_Prata2() {
                // Arrange: Configurar o carrinho com peso e produtos
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", new BigDecimal("500.00"), 4,
                                TipoProduto.ELETRONICO);
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", new BigDecimal("200.00"), 1,
                                TipoProduto.ROUPA);

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.PRATA));
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                // Assert: Verificar se o custo final está correto (com frete e desconto para
                // cliente Prata)
                assertEquals(new BigDecimal("635.000"), custoTotal);
        }

        @Test
        public void testCalcularCustoTotal_WithFrete_Ouro3() {
                // Arrange: Configurar o carrinho com produtos pesados
                Produto produto1 = new Produto(1L, "Produto 1", "Descrição do Produto 1", new BigDecimal("800.00"), 30,
                                TipoProduto.ELETRONICO); // Peso total de 30
                Produto produto2 = new Produto(2L, "Produto 2", "Descrição do Produto 2", new BigDecimal("500.00"), 30,
                                TipoProduto.ROUPA); // Peso total de 30

                ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
                ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

                CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
                carrinho.setCliente(new Cliente(TipoCliente.OURO)); // Cliente do tipo Ouro
                carrinho.setItens(Arrays.asList(item1, item2));

                // Act: Chamar o método calcularCustoTotal
                BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

                // Assert: Verificar se o custo final está correto (sem frete para cliente Ouro)
                assertThat(custoTotal).isEqualTo(new BigDecimal("1040.000"));
        }

        @Test
        public void testFinalizarCompra_CarrinhoNaoEncontrado() {
                // Arrange: Preparar os dados de teste
                Long carrinhoId = 1L;
                Long clienteId = 1L;

                // Configurando mocks
                when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(null); // Simula que
                                                                                                           // o carrinho
                                                                                                           // não foi
                                                                                                           // encontrado
                when(clienteService.buscarPorId(clienteId)).thenReturn(cliente); // Mock para cliente

                // Act: Chamar o método de finalizar compra
                CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);

                // Assert: Verificar o resultado
                assertFalse(resultado.sucesso(), "A compra deve ter falhado.");
                assertEquals("Carrinho não encontrado para o cliente.", resultado.mensagem(),
                                "A mensagem de erro deve ser a esperada.");
        }


}
