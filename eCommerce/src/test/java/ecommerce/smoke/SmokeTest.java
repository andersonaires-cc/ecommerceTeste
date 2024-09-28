package ecommerce.smoke;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ecommerce.dto.CompraDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ComponentScan(basePackages = "ecommerce")
public class SmokeTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void smokeTest_finalizar() {
		String url = "/compras/finalizar?carrinhoId=1&clienteId=1";

		ResponseEntity<CompraDTO> response = restTemplate.postForEntity(url, null, CompraDTO.class);

		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
	}
}
