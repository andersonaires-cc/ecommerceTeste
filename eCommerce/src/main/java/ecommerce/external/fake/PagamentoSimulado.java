package ecommerce.external.fake;

import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal{


    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        // Simulação da autorização do pagamento
        System.out.println("Pagamento autorizado para o cliente " + clienteId + " no valor de " + custoTotal);
        
        // Retornando um PagamentoDTO simulado
        Long transacaoIdSimulada = 123456789L; // Você pode gerar um ID de transação simulado
        return new PagamentoDTO(true, transacaoIdSimulada);
    }

    @Override
    public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId) {
        // Simulação do cancelamento do pagamento
        System.out.println("Pagamento cancelado para o cliente " + clienteId + " e transação " + pagamentoTransacaoId);
    }
}
