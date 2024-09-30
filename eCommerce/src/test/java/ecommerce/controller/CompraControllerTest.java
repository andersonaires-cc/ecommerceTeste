package ecommerce.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ecommerce.dto.CompraDTO;
import ecommerce.service.CompraService;

@ExtendWith(MockitoExtension.class)
public class CompraControllerTest {

    @InjectMocks
    private CompraController compraController;

    @Mock
    private CompraService compraService;

    private Long carrinhoId;
    private Long clienteId;

    @BeforeEach
    public void setup() {
        carrinhoId = 1L;
        clienteId = 1L;
    }

    @Test
    public void testFinalizarCompra_Sucesso() {
        // Arrange
        CompraDTO compraDTO = new CompraDTO(true, 123L, "Compra finalizada com sucesso."); // Alteração aqui
        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenReturn(compraDTO);

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(compraDTO, response.getBody());
    }

    @Test
    public void testFinalizarCompra_CarrinhoNaoEncontrado() {
        // Arrange
        when(compraService.finalizarCompra(carrinhoId, clienteId))
                .thenThrow(new IllegalStateException("Carrinho não encontrado."));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Carrinho não encontrado.", response.getBody().mensagem()); // Alteração aqui
    }

    @Test
    public void testFinalizarCompra_ItensIndisponiveis() {
        // Arrange
        when(compraService.finalizarCompra(carrinhoId, clienteId))
                .thenThrow(new IllegalStateException("Itens fora de estoque."));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Itens fora de estoque.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompra_PagamentoNaoAutorizado() {
        // Arrange
        when(compraService.finalizarCompra(carrinhoId, clienteId))
                .thenThrow(new IllegalStateException("Pagamento não autorizado."));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Pagamento não autorizado.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompra_ErroInterno() {
        // Arrange
        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenThrow(new RuntimeException("Erro inesperado."));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro ao processar compra.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompra_CarrinhoInvalido() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        // Simulando que o serviço lança IllegalArgumentException
        when(compraService.finalizarCompra(carrinhoId, clienteId))
                .thenThrow(new IllegalArgumentException("Carrinho inválido."));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Carrinho inválido.", response.getBody().mensagem());
    }

}
