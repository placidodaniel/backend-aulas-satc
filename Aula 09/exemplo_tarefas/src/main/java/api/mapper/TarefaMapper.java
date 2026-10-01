// Declara o pacote dos mappers -- as classes que convertem DTO <-> entidade.
package api.mapper;

// Importa os tipos de coleção usados na conversão de listas.
import java.util.Collection;
import java.util.List;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa os DTOs (o lado "contrato da API").
import api.dto.ResponsavelResponseDTO;
import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
// Importa as entidades (o lado "banco de dados").
import api.model.Responsavel;
import api.model.Tarefa;

// >>> MAPPER: o único lugar do projeto que sabe traduzir entre os dois mundos.
//
//   JSON ──Jackson──> TarefaRequestDTO ──toEntity()──> Tarefa ──save()──> banco
//   JSON <──Jackson── TarefaResponseDTO <──toResponse()── Tarefa <──find()── banco
//
// Na Aula 08, essa tradução estava espalhada: o TarefaService fazia
// "new Tarefa(null, dto.getTitulo(), ...)" no criar() e repetia os setters no
// atualizar(); na saída, quem traduzia era o Jackson, direto da entidade. Agora
// cada direção tem um método com nome, e o Service só chama.
//
// Mapeamento manual (escrito à mão, campo a campo) é verboso, mas não tem
// mágica: dá para ler, depurar com breakpoint e o compilador reclama se um
// campo mudar de tipo. Bibliotecas como o MapStruct geram este mesmo código
// sozinhas, a partir de uma interface (ver o desafio extra do EXERCICIOS.md).
//
// @Component: o Spring cria UM TarefaMapper e injeta no TarefaService pelo
// construtor, igual faz com os repositórios.
// Registra a classe como bean para poder ser injetada.
@Component
public class TarefaMapper {

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

    // PUT: copia os campos editáveis do DTO para uma entidade que JÁ EXISTE
    // (veio do banco). Não devolve nada -- altera o objeto recebido, e o
    // Service chama save() depois. id, concluida e dataCadastro não estão no
    // DTO, então continuam como estavam. O vínculo ENTRA na substituição: PUT
    // troca tudo, então sem responsavelId a tarefa fica sem responsável.
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

    // ---------------------------------------------------------------- saída

    // Entidade → DTO de saída. É daqui que sai o JSON de toda tarefa da API.
    public TarefaResponseDTO toResponse(Tarefa tarefa) {
        // Record se cria pelo construtor, com os valores na ordem dos componentes.
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.isConcluida(),
                tarefa.getResponsavel(),
                tarefa.getDataPrazo(),
                tarefa.getDataCadastro(),
                tarefa.getPrioridade(),
                // A entidade Responsavel também vira DTO antes de sair.
                toResponsavelResponse(tarefa.getResponsavelVinculado()));
    }

    // Converte uma lista inteira reaproveitando o toResponse() de um item.
    public List<TarefaResponseDTO> toResponseList(Collection<Tarefa> tarefas) {
        // stream().map(...): aplica toResponse em cada tarefa; toList() junta de novo.
        return tarefas.stream().map(this::toResponse).toList();
    }

    // O vínculo é opcional: tarefa sem responsável → "responsavelVinculado": null.
    // Está aqui dentro só porque ainda não existe um ResponsavelMapper
    // (Exercício 1 -- lá este método muda de casa).
    private ResponsavelResponseDTO toResponsavelResponse(Responsavel responsavel) {
        // Sem vínculo, não há o que converter.
        if (responsavel == null) {
            return null;
        }
        // Copia os três campos do contrato {id, nome, email}.
        return new ResponsavelResponseDTO(responsavel.getId(), responsavel.getNome(), responsavel.getEmail());
    }
}
