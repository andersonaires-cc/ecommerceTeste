package ecommerce.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.service.CarrinhoDeComprasService;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CarrinhoDeComprasServiceTest {

    @InjectMocks
    private CarrinhoDeComprasService carrinhoService;

    @Mock
    private CarrinhoDeComprasRepository repository;

    private Cliente cliente;
    private CarrinhoDeCompras carrinho;

    @BeforeEach
    public void setup() {
        // Configurar um cliente e um carrinho de compras de exemplo
        cliente = new Cliente(); // Inicializar com os parâmetros adequados
        cliente.setId(1L); // Defina um ID ou outros parâmetros se necessário

        carrinho = new CarrinhoDeCompras(); // Inicializar com os parâmetros adequados
        carrinho.setId(1L); // Defina um ID ou outros parâmetros se necessário
        carrinho.setCliente(cliente); // Defina o cliente associado
    }

    @Test
    public void testBuscarPorCarrinhoIdEClienteId_Sucesso() {
        Long carrinhoId = 1L;

        // Simular o comportamento do repositório
        when(repository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));

        // Chamar o método
        CarrinhoDeCompras resultado = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

        // Verificar o resultado
        assertNotNull(resultado);
        assertEquals(carrinho, resultado);
        verify(repository).findByIdAndCliente(carrinhoId, cliente);
    }

    @Test
    public void testBuscarPorCarrinhoIdEClienteId_CarrinhoNaoEncontrado() {
        Long carrinhoId = 1L;

        // Simular o comportamento do repositório
        when(repository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.empty());

        // Verificar a exceção lançada
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente));
        assertEquals("Carrinho não encontrado.", exception.getMessage());

        // Verificar que o repositório foi chamado
        verify(repository).findByIdAndCliente(carrinhoId, cliente);
    }
}
