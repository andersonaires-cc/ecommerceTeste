package ecommerce.external.fake;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

@Service
public class EstoqueSimulado implements IEstoqueExternal {

    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
        // Simulação da baixa no estoque
        // Aqui você pode adicionar a lógica para simular a baixa
        System.out.println("Baixa no estoque para os produtos: " + produtosIds + " com quantidades: " + produtosQuantidades);
        
        // Retornando um EstoqueBaixaDTO simulado
        return new EstoqueBaixaDTO(true); // Supondo que a baixa foi bem-sucedida
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
        // Simulação da verificação de disponibilidade
        List<Long> produtosIndisponiveis = new ArrayList<>(); // Lista simulada de produtos indisponíveis

        // Aqui você pode adicionar a lógica para verificar a disponibilidade
        // Adicionando algum produto à lista de indisponíveis para simulação
        if (!produtosIds.isEmpty()) {
            produtosIndisponiveis.add(produtosIds.get(0)); // Simulando que o primeiro produto está indisponível
        }
        
        // Retornando um DisponibilidadeDTO simulado
        return new DisponibilidadeDTO(produtosIndisponiveis.isEmpty(), produtosIndisponiveis);
    }    
}
