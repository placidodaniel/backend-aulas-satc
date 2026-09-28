// Declara o pacote da classe DTO.
package api.dto;

// Importa a validação de formato de e-mail.
import jakarta.validation.constraints.Email;
// Importa a validação que rejeita texto nulo, vazio ou só com espaços.
import jakarta.validation.constraints.NotBlank;

// Exercício 6: corpo do POST /responsaveis. Não tem "id": quem gera é o banco.
public class ResponsavelDTO {

    // Armazena o nome enviado pelo cliente.
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    // @Email sozinho aceita null e "" -- por isso vem junto do @NotBlank.
    // Armazena o e-mail enviado pelo cliente.
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    // Devolve o nome recebido no DTO.
    public String getNome() {
        return nome;
    }

    // Recebe e guarda um novo nome.
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Devolve o e-mail recebido no DTO.
    public String getEmail() {
        return email;
    }

    // Recebe e guarda um novo e-mail.
    public void setEmail(String email) {
        this.email = email;
    }
}
