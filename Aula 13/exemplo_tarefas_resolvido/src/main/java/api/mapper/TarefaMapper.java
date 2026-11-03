// Declara o pacote dos mappers -- as classes que convertem DTO <-> entidade.
package api.mapper;

// Importa o tipo de data usado nos campos calculados.
import java.time.LocalDate;
// Importa a unidade de tempo usada para contar dias entre duas datas.
import java.time.temporal.ChronoUnit;
// Importa os tipos de coleção usados na conversão de listas.
import java.util.Collection;
import java.util.List;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa os DTOs (o lado "contrato da API").
import api.dto.TarefaPatchDTO;
import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
import api.dto.TarefaResumoDTO;
// Importa as entidades (o lado "banco de dados").
import api.model.Responsavel;
import api.model.Tarefa;

// >>> MAPPER: o único lugar do projeto que sabe traduzir entre os dois mundos.
//
//   JSON ──Jackson──> TarefaRequestDTO ──toEntity()──> Tarefa ──save()──> banco
//   JSON <──Jackson── TarefaResponseDTO <──toResponse()── Tarefa <──find()── banco
//
// @Component: o Spring cria UM TarefaMapper e injeta no TarefaService (e, no
// Exercício 4, também no ResponsavelService) pelo construtor.
// Registra a classe como bean para poder ser injetada.
@Component
public class TarefaMapper {

    // Exercício 1: a conversão de Responsavel mora no ResponsavelMapper; este
    // mapper só pede emprestado. Uma regra, um lugar.
    private final ResponsavelMapper responsavelMapper;

    // Recebe o ResponsavelMapper injetado pelo Spring.
    public TarefaMapper(ResponsavelMapper responsavelMapper) {
        // Guarda a dependência para usar em toResponse().
        this.responsavelMapper = responsavelMapper;
    }

    // ---------------------------------------------------------------- entrada

    // POST: DTO de entrada (já validado pelo @Valid) → entidade NOVA.
    // Não recebe id: é null aqui e o banco gera no INSERT. concluida=false e
    // dataCadastro=hoje são decididos pelo construtor da própria entidade.
    //
    // "responsavel" chega PRONTO: o DTO só traz o responsavelId, e transformar
    // id em entidade exige o banco -- trabalho do TarefaService, não do mapper.
    // O mapper só converte; ele não consulta nada. null = tarefa sem vínculo.
    public Tarefa toEntity(TarefaRequestDTO dto, Responsavel responsavel) {
        // Record não tem getTitulo(): o "getter" tem o nome do componente, titulo().
        Tarefa tarefa = new Tarefa(null, dto.titulo(), dto.responsavel(), dto.dataPrazo(), dto.prioridade());
        // Liga a tarefa ao responsável já buscado pelo Service (ou deixa sem vínculo).
        tarefa.setResponsavelVinculado(responsavel);
        return tarefa;
    }

    // PUT: copia todos os campos editáveis do DTO para uma entidade que já
    // existe. id, concluida e dataCadastro continuam como estavam. O vínculo
    // ENTRA na substituição: PUT troca tudo, então sem responsavelId a tarefa
    // fica sem responsável.
    public void updateEntity(Tarefa tarefa, TarefaRequestDTO dto, Responsavel responsavel) {
        // Substitui o título antigo pelo novo valor.
        tarefa.setTitulo(dto.titulo());
        // Substitui o responsável (texto) antigo pelo novo valor.
        tarefa.setResponsavel(dto.responsavel());
        // Substitui o prazo antigo pelo novo valor.
        tarefa.setDataPrazo(dto.dataPrazo());
        // Substitui a prioridade antiga pelo novo valor.
        tarefa.setPrioridade(dto.prioridade());
        // Substitui o vínculo (o responsável chega pronto do Service, ou null).
        tarefa.setResponsavelVinculado(responsavel);
    }

    // Exercício 3 -- PATCH: copia SÓ os campos que vieram no JSON. null quer
    // dizer "o cliente não mandou" e o valor atual da entidade é mantido.
    // "responsavel" chega pronto do Service quando veio responsavelId; null =
    // não mexer no vínculo (o PATCH não tem como pedir "desvincular").
    public void applyPatch(Tarefa tarefa, TarefaPatchDTO dto, Responsavel responsavel) {
        // Cada campo é conferido separadamente: {} não altera nada.
        if (dto.titulo() != null) {
            tarefa.setTitulo(dto.titulo());
        }
        // Só troca o responsável (texto) se ele veio no JSON.
        if (dto.responsavel() != null) {
            tarefa.setResponsavel(dto.responsavel());
        }
        // Só troca o prazo se ele veio -- e, se veio, já passou pelo @FutureOrPresent.
        if (dto.dataPrazo() != null) {
            tarefa.setDataPrazo(dto.dataPrazo());
        }
        // Integer: null = ausente. Um int nunca seria null e não daria para saber.
        if (dto.prioridade() != null) {
            tarefa.setPrioridade(dto.prioridade());
        }
        // Só troca o vínculo se veio um responsavelId (já resolvido pelo Service).
        if (responsavel != null) {
            tarefa.setResponsavelVinculado(responsavel);
        }
    }

    // ---------------------------------------------------------------- saída

    // Entidade → DTO de saída. É daqui que sai o JSON de toda tarefa da API.
    public TarefaResponseDTO toResponse(Tarefa tarefa) {
        // Exercício 2: campos calculados na hora, a partir do prazo e do estado.
        // DAYS.between(hoje, prazo): positivo = faltam dias; negativo = passou.
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), tarefa.getDataPrazo());
        // Mesma regra do findByConcluidaFalseAndDataPrazoBefore(hoje): pendente
        // e com prazo ANTES de hoje (vencer hoje ainda não é atraso).
        boolean atrasada = !tarefa.isConcluida() && diasRestantes < 0;

        // Record se cria pelo construtor, com os valores na ordem dos componentes.
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.isConcluida(),
                tarefa.getResponsavel(),
                tarefa.getDataPrazo(),
                tarefa.getDataCadastro(),
                tarefa.getPrioridade(),
                diasRestantes,
                atrasada,
                // Exercício 1: delega ao ResponsavelMapper (que já trata o null).
                responsavelMapper.toResponse(tarefa.getResponsavelVinculado()));
    }

    // Converte uma lista inteira reaproveitando o toResponse() de um item.
    public List<TarefaResponseDTO> toResponseList(Collection<Tarefa> tarefas) {
        // stream().map(...): aplica toResponse em cada tarefa; toList() junta de novo.
        return tarefas.stream().map(this::toResponse).toList();
    }

    // Exercício 4: versão enxuta, para aparecer dentro do ResponsavelDetalheDTO.
    public TarefaResumoDTO toResumo(Tarefa tarefa) {
        // Só os quatro campos do resumo -- sem o responsável vinculado.
        return new TarefaResumoDTO(tarefa.getId(), tarefa.getTitulo(), tarefa.isConcluida(), tarefa.getDataPrazo());
    }

    // Exercício 4: converte a lista de tarefas de um responsável em resumos.
    public List<TarefaResumoDTO> toResumoList(Collection<Tarefa> tarefas) {
        // Mesmo padrão do toResponseList(), com o toResumo() de cada item.
        return tarefas.stream().map(this::toResumo).toList();
    }
}
