package api.seguranca;

import java.time.Instant;

// Aula 15: muitas tentativas erradas seguidas. Vira 429 Too Many Requests,
// com o cabeçalho Retry-After dizendo quantos segundos esperar.
public class ContaBloqueadaException extends RuntimeException {

    private final Instant ate;

    public ContaBloqueadaException(Instant ate) {
        super("Muitas tentativas erradas. Conta bloqueada temporariamente; tente de novo mais tarde.");
        this.ate = ate;
    }

    public Instant getAte() {
        return ate;
    }
}
