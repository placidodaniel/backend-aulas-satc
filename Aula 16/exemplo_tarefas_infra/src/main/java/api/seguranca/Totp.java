// Pacote da autenticação e da segurança (Aula 15).
package api.seguranca;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// =====================================================================
// AULA 15: TOTP, o código de 6 dígitos do aplicativo autenticador.
// =====================================================================
//
// É o que o Google Authenticator, o Microsoft Authenticator, o Authy, o
// 2FAS e o FreeOTP fazem. Está nos padrões RFC 4226 (HOTP) e RFC 6238 (TOTP).
// Escrevemos à mão, sem biblioteca, porque é curto e porque dá para ver que
// não tem mágica:
//
//   1. Na ativação, o servidor sorteia um SEGREDO (20 bytes aleatórios) e o
//      entrega ao celular uma única vez, pelo QR Code.
//   2. Servidor e celular olham o RELÓGIO e calculam o "passo":
//      passo = segundos desde 1970 / 30. Muda a cada 30 segundos.
//   3. Os dois calculam HMAC-SHA1(segredo, passo) e tiram 6 dígitos do resultado.
//
// Mesmo segredo + mesmo relógio = mesmo código, sem internet nenhuma no
// celular. Quem não tem o segredo (que está só no celular e no nosso banco)
// não consegue gerar o código, mesmo sabendo a senha.
public final class Totp {

    // Duração de cada código, em segundos (o padrão que todo aplicativo usa).
    public static final int PERIODO = 30;
    // Quantos dígitos tem o código.
    public static final int DIGITOS = 6;
    // Tolerância: aceitamos o passo anterior e o seguinte também, porque o
    // relógio do celular pode estar alguns segundos adiantado ou atrasado.
    private static final int JANELA = 1;

    private static final String ALFABETO_BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    // SecureRandom, e nunca Random: o segredo não pode ser adivinhável.
    private static final SecureRandom SORTEIO = new SecureRandom();

    private Totp() {
    }

    // Sorteia um segredo de 160 bits (o tamanho recomendado para HMAC-SHA1) em Base32.
    public static String novoSegredo() {
        byte[] bytes = new byte[20];
        SORTEIO.nextBytes(bytes);
        return base32(bytes);
    }

    // O passo de 30 segundos de um instante.
    public static long passo(Instant instante) {
        return instante.getEpochSecond() / PERIODO;
    }

    // O código de 6 dígitos de um passo (com zeros à esquerda: "004211").
    public static String codigo(String segredoBase32, long passo) {
        try {
            // 1. HMAC-SHA1 do passo (8 bytes) com o segredo como chave.
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(deBase32(segredoBase32), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());

            // 2. "Truncamento dinâmico" (RFC 4226): os 4 últimos bits do hash dizem de
            //    onde tirar 4 bytes; o primeiro bit é zerado para o número ser positivo.
            int inicio = hash[hash.length - 1] & 0x0f;
            int numero = ((hash[inicio] & 0x7f) << 24)
                    | ((hash[inicio + 1] & 0xff) << 16)
                    | ((hash[inicio + 2] & 0xff) << 8)
                    | (hash[inicio + 3] & 0xff);

            // 3. Os 6 últimos dígitos.
            return String.format("%0" + DIGITOS + "d", numero % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 indisponível na JVM", e);
        }
    }

    // Confere um código digitado. Devolve o PASSO que bateu (para o chamador
    // gravar e impedir que o mesmo código seja usado de novo) ou vazio.
    public static OptionalLong verificar(String segredoBase32, String codigoDigitado, Instant agora) {
        if (codigoDigitado == null || !codigoDigitado.matches("\\d{" + DIGITOS + "}")) {
            return OptionalLong.empty();
        }
        long atual = passo(agora);
        for (long p = atual - JANELA; p <= atual + JANELA; p++) {
            // MessageDigest.isEqual compara em tempo CONSTANTE: o tempo de resposta não
            // revela quantos dígitos estavam certos (o equals() para no primeiro diferente).
            if (MessageDigest.isEqual(codigo(segredoBase32, p).getBytes(), codigoDigitado.getBytes())) {
                return OptionalLong.of(p);
            }
        }
        return OptionalLong.empty();
    }

    // O endereço que vira QR Code. O aplicativo lê e cadastra a conta sozinho:
    // otpauth://totp/Emissor:email?secret=...&issuer=Emissor&algorithm=SHA1&digits=6&period=30
    public static String uri(String emissor, String conta, String segredoBase32) {
        String rotulo = codificar(emissor) + ":" + codificar(conta);
        return "otpauth://totp/" + rotulo + "?secret=" + segredoBase32 + "&issuer=" + codificar(emissor)
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PERIODO;
    }

    private static String codificar(String texto) {
        return java.net.URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    // ------------------------------------------------------------ Base32 (RFC 4648)
    // Os aplicativos esperam o segredo em Base32: só letras maiúsculas e 2 a 7,
    // fácil de digitar à mão se a câmera não ler o QR Code.

    static String base32(byte[] dados) {
        StringBuilder saida = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : dados) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                saida.append(ALFABETO_BASE32.charAt((buffer >> (bits - 5)) & 0x1f));
                bits -= 5;
            }
        }
        if (bits > 0) {
            saida.append(ALFABETO_BASE32.charAt((buffer << (5 - bits)) & 0x1f));
        }
        return saida.toString();
    }

    static byte[] deBase32(String texto) {
        String limpo = texto.replace("=", "").replace(" ", "").toUpperCase();
        ByteBuffer saida = ByteBuffer.allocate(limpo.length() * 5 / 8);
        int buffer = 0;
        int bits = 0;
        for (char c : limpo.toCharArray()) {
            int valor = ALFABETO_BASE32.indexOf(c);
            if (valor < 0) {
                throw new IllegalArgumentException("Caractere inválido em Base32: " + c);
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                saida.put((byte) ((buffer >> (bits - 8)) & 0xff));
                bits -= 8;
            }
        }
        return saida.array();
    }
}
