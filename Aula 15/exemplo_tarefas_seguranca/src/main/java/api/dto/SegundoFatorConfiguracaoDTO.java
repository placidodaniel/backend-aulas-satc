package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Aula 15: o que a tela precisa para a pessoa cadastrar a conta no aplicativo.
// Aparece UMA vez: depois de ativado, o segredo nunca mais sai da API.
@Schema(description = "Dados para ler no aplicativo autenticador")
public record SegundoFatorConfiguracaoDTO(
        @Schema(description = "O segredo em Base32, para digitar à mão se a câmera não ler o QR Code")
        String segredo,
        @Schema(description = "O endereço otpauth:// que está dentro do QR Code")
        String uri,
        @Schema(description = "O QR Code como imagem SVG")
        String qrCodeSvg
) {
}
