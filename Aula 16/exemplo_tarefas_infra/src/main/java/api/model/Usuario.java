// Define o pacote onde as entidades ficam organizadas.
package api.model;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Aula 15: uma pessoa que pode entrar no sistema (tabela criada pela migration V4).
//
// Repare no que NÃO existe aqui: um campo "senha". A entidade só conhece o HASH
// da senha. Se o banco vazar, quem pegar a tabela não tem as senhas, só hashes
// BCrypt, que são lentos de propósito para atacar por tentativa e erro.
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    // Sempre guardado em minúsculas (veja AutenticacaoService.normalizar).
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 60)
    private String senhaHash;

    // Segredo do aplicativo autenticador, em Base32 (o formato que o app entende).
    @Column(name = "totp_segredo", length = 64)
    private String totpSegredo;

    @Column(name = "totp_ativo", nullable = false)
    private boolean totpAtivo;

    @Column(name = "totp_ultimo_passo", nullable = false)
    private long totpUltimoPasso;

    @Column(name = "tentativas_falhas", nullable = false)
    private int tentativasFalhas;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    // Exigido pelo Hibernate.
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Instant criadoEm) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
    }

    // ---------------------------------------------------------- força bruta

    // A conta está bloqueada AGORA?
    public boolean bloqueadoEm(Instant agora) {
        return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
    }

    // Conta um erro. Ao chegar no limite, bloqueia até "agora + duracao" e zera a contagem.
    public void registrarFalha(int limite, Instant agora, Duration duracao) {
        tentativasFalhas++;
        if (tentativasFalhas >= limite) {
            bloqueadoAte = agora.plus(duracao);
            tentativasFalhas = 0;
        }
    }

    // Acertou: esquece os erros anteriores.
    public void registrarSucesso() {
        tentativasFalhas = 0;
        bloqueadoAte = null;
    }

    // ---------------------------------------------------------- segundo fator

    // Guarda um segredo NOVO, ainda não ativo (a pessoa precisa provar que leu o QR Code).
    public void prepararSegundoFator(String segredo) {
        this.totpSegredo = segredo;
        this.totpAtivo = false;
        this.totpUltimoPasso = 0;
    }

    public void ativarSegundoFator() {
        this.totpAtivo = true;
    }

    public void desativarSegundoFator() {
        this.totpSegredo = null;
        this.totpAtivo = false;
        this.totpUltimoPasso = 0;
    }

    // Anota o passo de 30 s que acabou de ser usado (contra reutilização do código).
    public void usarPasso(long passo) {
        this.totpUltimoPasso = passo;
    }

    // ---------------------------------------------------------- getters

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getTotpSegredo() {
        return totpSegredo;
    }

    public boolean isTotpAtivo() {
        return totpAtivo;
    }

    public long getTotpUltimoPasso() {
        return totpUltimoPasso;
    }

    public int getTentativasFalhas() {
        return tentativasFalhas;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }
}
