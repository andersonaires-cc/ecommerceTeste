package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestExecutionListeners;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.service.CompraService;

public class CompraServiceTest {

    @InjectMocks
    private CompraService compraService;

    @Mock
    private CarrinhoDeCompras carrinho;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    
        // Assert: Verificar se o custo final está correto (com frete e desconto para cliente Prata)
        assertEquals(new BigDecimal("726.000"), custoTotal);
    }
    

    @Test
    public void testCalcularCustoTotal_WithFrete_Bronze() {
        // Arrange: Configurar o carrinho com peso e produtos
        Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", new BigDecimal("800.00"), 7,
                TipoProduto.ELETRONICO);
        Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", new BigDecimal("400.00"), 3, TipoProduto.ROUPA);

        ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
        ItemCompra item2 = new ItemCompra(2L, produto2, 1L); // Apenas um produto de cada

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(new Cliente(TipoCliente.BRONZE));
        carrinho.setItens(Arrays.asList(item1, item2));

        // Act: Chamar o método calcularCustoTotal
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        assertEquals(new BigDecimal("960.000"), custoTotal);
    }

}
