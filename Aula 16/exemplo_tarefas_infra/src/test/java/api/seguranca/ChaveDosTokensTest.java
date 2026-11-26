package api.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Aula 16: a chave dos tokens em cada ambiente.
// Com várias cópias da API, TODAS precisam da mesma chave. Sortear uma chave por cópia
// faz o token de uma ser recusado pelas outras (401 "aleatório", difícil de entender).
class ChaveDosTokensTest {

    private final SegurancaConfig config = new SegurancaConfig();

    @Test
    @DisplayName("aula: sem JWT_SEGREDO, sorteia uma chave (cada subida, uma diferente)")
    void semSegredoEmAulaSorteia() {
        var primeira = config.chaveDosTokens("", false);
        var segunda = config.chaveDosTokens("", false);

        assertThat(primeira.getEncoded()).hasSize(32);
        assertThat(primeira.getEncoded()).isNotEqualTo(segunda.getEncoded());
    }

    @Test
    @DisplayName("produção: sem JWT_SEGREDO (ou com ele vazio), a API não sobe")
    void semSegredoEmProducaoFalha() {
        assertThatThrownBy(() -> config.chaveDosTokens("", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SEGREDO");
        assertThatThrownBy(() -> config.chaveDosTokens(null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("com a mesma JWT_SEGREDO, duas cópias chegam à mesma chave")
    void mesmoSegredoMesmaChave() {
        String segredo = "uma-chave-de-teste-com-mais-de-32-caracteres";

        assertThat(config.chaveDosTokens(segredo, true).getEncoded())
                .isEqualTo(config.chaveDosTokens(segredo, false).getEncoded());
    }
}
