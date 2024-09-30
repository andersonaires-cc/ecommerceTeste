package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ecommerce.entity.Cliente;
import ecommerce.entity.TipoCliente;
import ecommerce.repository.ClienteRepository;

public class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuscarPorId_ClienteExistente() {
        // Arrange: Criar um cliente simulado e configurar o repositório
        Long clienteId = 1L;
        Cliente cliente = new Cliente(clienteId, "Cliente Teste", "Endereço Teste", TipoCliente.PRATA);
        
        when(clienteRepository.findById(clienteId)).thenReturn(java.util.Optional.of(cliente));

        // Act: Chamar o método a ser testado
        Cliente resultado = clienteService.buscarPorId(clienteId);

        // Assert: Verificar se o resultado é o esperado
        assertEquals(cliente, resultado);
    }

    @Test
    public void testBuscarPorId_ClienteNaoExistente() {
        // Arrange: Configurar o repositório para retornar um Optional vazio
        when(clienteRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        // Act & Assert: Verificar se a exceção é lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.buscarPorId(1L);
        });

        // Assert: Verificar a mensagem da exceção
        assertEquals("Cliente não encontrado", exception.getMessage());
    }
}
