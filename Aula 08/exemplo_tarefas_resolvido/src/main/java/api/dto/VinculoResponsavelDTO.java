// Declara o pacote da classe DTO.
package api.dto;

// Importa a validação que rejeita valores nulos.
import jakarta.validation.constraints.NotNull;

// Exercício 6: corpo do PUT /tarefas/{id}/responsavel -- só o id do
// responsável. O nome e o e-mail já estão no banco; o cliente não precisa
// (nem deve) reenviá-los.
public class VinculoResponsavelDTO {

    // Armazena o id do responsável que será ligado à tarefa.
    @NotNull(message = "responsavelId é obrigatório")
    private Long responsavelId;

    // Devolve o id recebido no DTO.
    public Long getResponsavelId() {
        return responsavelId;
    }

    // Recebe e guarda o id do responsável.
    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }
}
