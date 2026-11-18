package api.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Aula 15: testes do TOTP. Os valores esperados vêm do próprio padrão (RFC 6238,
// Apêndice B): se o nosso código bate com a tabela oficial, bate com qualquer
// aplicativo autenticador.
class TotpTest {

    // O segredo de teste da RFC: os bytes do texto "12345678901234567890".
    private static final String SEGREDO_DA_RFC = Totp.base32("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    @Test
    @DisplayName("Bate com a tabela oficial da RFC 6238 (os 6 últimos dígitos)")
    void tabelaDaRfc() {
        // RFC 6238: T = 59 s -> 94287082; T = 1111111109 -> 07081804; T = 2000000000 -> 69279037
        assertThat(Totp.codigo(SEGREDO_DA_RFC, Totp.passo(Instant.ofEpochSecond(59)))).isEqualTo("287082");
        assertThat(Totp.codigo(SEGREDO_DA_RFC, Totp.passo(Instant.ofEpochSecond(1111111109)))).isEqualTo("081804");
        assertThat(Totp.codigo(SEGREDO_DA_RFC, Totp.passo(Instant.ofEpochSecond(2000000000)))).isEqualTo("279037");
    }

    @Test
    @DisplayName("Base32 vai e volta sem perder nada")
    void base32() {
        assertThat(Totp.base32("foobar".getBytes())).isEqualTo("MZXW6YTBOI"); // exemplo da RFC 4648
        assertThat(Totp.deBase32("MZXW6YTBOI")).isEqualTo("foobar".getBytes());
        assertThat(Totp.novoSegredo()).hasSize(32).matches("[A-Z2-7]+");
    }

    @Test
    @DisplayName("Aceita o código atual e os vizinhos (relógio do celular adiantado ou atrasado)")
    void janela() {
        String segredo = Totp.novoSegredo();
        Instant agora = Instant.parse("2026-11-18T12:00:10Z");
        long passo = Totp.passo(agora);

        assertThat(Totp.verificar(segredo, Totp.codigo(segredo, passo), agora)).hasValue(passo);
        assertThat(Totp.verificar(segredo, Totp.codigo(segredo, passo - 1), agora)).hasValue(passo - 1);
        assertThat(Totp.verificar(segredo, Totp.codigo(segredo, passo + 1), agora)).hasValue(passo + 1);
        // Dois minutos atrás: já não vale.
        assertThat(Totp.verificar(segredo, Totp.codigo(segredo, passo - 4), agora)).isEmpty();
    }

    @Test
    @DisplayName("Recusa formato errado sem nem calcular")
    void formato() {
        String segredo = Totp.novoSegredo();
        Instant agora = Instant.now();
        assertThat(Totp.verificar(segredo, null, agora)).isEmpty();
        assertThat(Totp.verificar(segredo, "12345", agora)).isEmpty();
        assertThat(Totp.verificar(segredo, "abcdef", agora)).isEmpty();
    }

    @Test
    @DisplayName("O endereço otpauth:// tem o formato que os aplicativos leem")
    void uri() {
        assertThat(Totp.uri("Tarefas SATC", "ana@satc.edu.br", "ABC234"))
                .isEqualTo("otpauth://totp/Tarefas%20SATC:ana%40satc.edu.br?secret=ABC234&issuer=Tarefas%20SATC"
                        + "&algorithm=SHA1&digits=6&period=30");
    }
}
