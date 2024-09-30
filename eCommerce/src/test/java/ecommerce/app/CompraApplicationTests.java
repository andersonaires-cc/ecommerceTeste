package ecommerce.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ecommerce.CompraApplication;

@SpringBootTest
class CompraApplicationTests {

    @Test
    void contextLoads() {
        // Verifica se o contexto da aplicação carrega sem erros.
    }
      @Test
    void main() {
        // Chamando o método main da aplicação
        CompraApplication.main(new String[] {});
    }
}
