// Define o pacote da camada de regras de negócio.
package api.service;

// Importa o tipo de data usado na consulta de atrasadas.
import java.time.LocalDate;
// Importa a lista devolvida nas consultas.
import java.util.List;
// Importa o "dicionário" dia → previsão (Exercício 5 da Aula 12).
import java.util.Map;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa os DTOs de entrada e de saída.
import api.dto.PrevisaoDTO;
import api.dto.TarefaComPrevisaoDTO;
import api.dto.TarefaPatchDTO;
import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
// Importa o mapper que traduz DTO <-> entidade.
import api.mapper.TarefaMapper;
// Importa as entidades persistidas.
import api.model.Responsavel;
import api.model.Tarefa;
// Importa os repositórios usados pelo service.
import api.repository.ResponsavelRepository;
import api.repository.TarefaRepository;

// @Service: camada de regra de negócio -- o Controller fala com o Service,
// nunca direto com o Repository.
//
// >>> O QUE MUDOU NA AULA 09: a "fronteira" das entidades.
//   - Entra: TarefaRequestDTO (ou só ids).
//   - Sai:   TarefaResponseDTO.
//   - A entidade Tarefa vive só DAQUI PARA BAIXO (Service → Repository → banco).
// O Controller não importa mais api.model -- ele nem sabe que Tarefa existe.
//
// Por que converter aqui, e não no Controller? Porque é o Service que conversa
// com o banco. Se um dia houver um relacionamento LAZY, basta marcar o método
// com @Transactional para a conversão rodar com a sessão do Hibernate aberta.
// No Controller isso não é possível: com spring.jpa.open-in-view=false, a
// sessão já fechou quando o Service devolve.
// Registra a classe como bean da camada de serviço.
@Service
public class TarefaService {

    // Acesso à tabela tarefas.
    private final TarefaRepository repository;

    // Usado para buscar o responsável antes de vincular.
    private final ResponsavelRepository responsavelRepository;

    // Aula 09: converte DTO <-> entidade. Injetado pelo Spring, igual aos repositórios.
    private final TarefaMapper mapper;

    // Aula 12, Exercícios 2 e 5: a previsão do tempo para os prazos.
    private final PrevisaoService previsaoService;

    // Aula 12, Exercício 5: chance de chuva a partir da qual o dia conta como "de chuva".
    public static final int CHANCE_DE_CHUVA = 60;

    // Recebe as dependências que o Spring injeta automaticamente.
    public TarefaService(TarefaRepository repository, ResponsavelRepository responsavelRepository, TarefaMapper mapper,
                         PrevisaoService previsaoService) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.responsavelRepository = responsavelRepository;
        this.mapper = mapper;
        this.previsaoService = previsaoService;
    }

    // Aula 12, Exercício 2: a tarefa com a previsão do tempo para o prazo dela.
    // Dado próprio (a tarefa, do banco) + dado externo (a previsão, do Open-Meteo).
    public TarefaComPrevisaoDTO buscarComPrevisao(Long id, double latitude, double longitude) {
        // Coordenada inválida é erro de quem chamou, tenha previsão ou não (400).
        previsaoService.validarCoordenadas(latitude, longitude);
        // 404 se a tarefa não existir.
        Tarefa tarefa = buscarEntidade(id);
        TarefaResponseDTO dto = mapper.toResponse(tarefa);
        LocalDate prazo = tarefa.getDataPrazo();
        // FALLBACK: sem previsão para o prazo, a tarefa vem do mesmo jeito, com um aviso.
        if (!previsaoService.temPrevisao(prazo)) {
            String aviso = prazo.isBefore(LocalDate.now())
                    ? "O prazo (" + prazo + ") já passou: não há previsão para dias passados"
                    : "Ainda não há previsão para o prazo (" + prazo + "): ela só cobre até "
                            + LocalDate.now().plusDays(PrevisaoService.DIAS_DE_PREVISAO);
            return new TarefaComPrevisaoDTO(dto, null, aviso);
        }
        // Dentro da janela: a mesma previsão do GET /previsao (inclusive o cache).
        return new TarefaComPrevisaoDTO(dto, previsaoService.buscar(latitude, longitude, prazo), null);
    }

    // Aula 12, Exercício 5: tarefas pendentes cujo prazo cai num dia de chuva.
    // UMA chamada ao Open-Meteo para todas, e não uma por tarefa.
    public List<TarefaComPrevisaoDTO> listarEmDiasDeChuva(double latitude, double longitude) {
        LocalDate hoje = LocalDate.now();
        // 1. Do banco: as pendentes com prazo dentro da janela da previsão.
        List<Tarefa> pendentes = repository.findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc(
                hoje, hoje.plusDays(PrevisaoService.DIAS_DE_PREVISAO));
        // Sem tarefa, nem chama o Open-Meteo.
        if (pendentes.isEmpty()) {
            return List.of();
        }
        // 2. Do Open-Meteo: os 16 dias de uma vez, num dicionário dia → previsão.
        Map<LocalDate, PrevisaoDTO> previsaoPorDia = previsaoService.buscarJanela(latitude, longitude);
        // 3. Cruza os dois: fica só quem tem chance de chuva de 60% ou mais no prazo.
        return pendentes.stream()
                .filter(tarefa -> {
                    PrevisaoDTO previsao = previsaoPorDia.get(tarefa.getDataPrazo());
                    return previsao != null && previsao.chanceDeChuva() != null
                            && previsao.chanceDeChuva() >= CHANCE_DE_CHUVA;
                })
                .map(tarefa -> new TarefaComPrevisaoDTO(mapper.toResponse(tarefa),
                        previsaoPorDia.get(tarefa.getDataPrazo()), null))
                .toList();
    }

    // Lista todas as tarefas, prazo mais próximo primeiro.
    public List<TarefaResponseDTO> listarTodas() {
        // O Repository devolve entidades; o mapper converte a lista inteira.
        return mapper.toResponseList(repository.findAllByOrderByDataPrazoAsc());
    }

    // Tarefas cujo responsável (texto) bate com o nome, sem diferenciar
    // maiúsculas/minúsculas. Nenhum resultado = lista vazia.
    public List<TarefaResponseDTO> buscarPorResponsavel(String responsavel) {
        // O filtro roda no PostgreSQL; a conversão, aqui.
        return mapper.toResponseList(repository.findByResponsavelIgnoreCase(responsavel));
    }

    // Tarefas não concluídas cujo prazo já passou.
    public List<TarefaResponseDTO> listarAtrasadas() {
        // "Before" é estrito: prazo = hoje ainda não está atrasado.
        return mapper.toResponseList(repository.findByConcluidaFalseAndDataPrazoBefore(LocalDate.now()));
    }

    // Cria uma tarefa a partir do DTO já validado.
    public TarefaResponseDTO criar(TarefaRequestDTO dto) {
        // 1. O responsavelId (se veio) vira entidade -- 404 se não existir.
        Responsavel responsavel = buscarResponsavel(dto.responsavelId());
        // 2. DTO + responsável → entidade nova (id ainda null), já vinculada.
        Tarefa tarefa = mapper.toEntity(dto, responsavel);
        // 3. O INSERT acontece aqui (com responsavel_id); a entidade volta com id.
        Tarefa salva = repository.save(tarefa);
        // 4. Entidade → DTO de saída (o vínculo sai como objeto {id, nome, email}).
        return mapper.toResponse(salva);
    }

    // Busca uma tarefa pelo id -- 404 (via exceção) se não existir.
    public TarefaResponseDTO buscarPorId(Long id) {
        // Reaproveita a busca interna e só converte o resultado.
        return mapper.toResponse(buscarEntidade(id));
    }

    // Remove a tarefa identificada pelo id.
    public void remover(Long id) {
        // Confere a existência antes de tentar excluir.
        if (!repository.existsById(id)) {
            // Mantém o contrato da API retornando 404 para id inexistente.
            throw new TarefaNaoEncontradaException(id);
        }
        // Executa o DELETE gerado pelo Spring Data JPA.
        repository.deleteById(id);
    }

    // Atualiza título, responsável, prazo, prioridade e vínculo de uma tarefa
    // existente. PUT substitui tudo: sem responsavelId, o vínculo é removido.
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO dto) {
        // Busca a entidade que já está no banco (ou lança 404).
        Tarefa tarefa = buscarEntidade(id);
        // O mapper copia os campos editáveis do DTO -- e o responsável já resolvido.
        mapper.updateEntity(tarefa, dto, buscarResponsavel(dto.responsavelId()));
        // Persiste (UPDATE) e devolve a versão atualizada como DTO.
        return mapper.toResponse(repository.save(tarefa));
    }

    // Exercício 3 -- PATCH: altera só os campos que vieram no JSON.
    public TarefaResponseDTO atualizarParcial(Long id, TarefaPatchDTO dto) {
        // Mesma busca do PUT: 404 se o id não existir.
        Tarefa tarefa = buscarEntidade(id);
        // O mapper copia só o que não for null; o resto da entidade fica igual.
        // Se veio responsavelId, o Service resolve antes (404 se não existir).
        mapper.applyPatch(tarefa, dto, buscarResponsavel(dto.responsavelId()));
        // O Hibernate gera o UPDATE; a resposta é a tarefa inteira, já alterada.
        return mapper.toResponse(repository.save(tarefa));
    }

    // Bônus -- alterna o campo concluida entre true e false.
    public TarefaResponseDTO alternarConcluida(Long id) {
        // Busca a entidade existente ou lança 404.
        Tarefa tarefa = buscarEntidade(id);
        // Inverte o valor atual do booleano.
        tarefa.setConcluida(!tarefa.isConcluida());
        // Persiste o novo estado e devolve como DTO.
        return mapper.toResponse(repository.save(tarefa));
    }

    // Liga a tarefa a um responsável cadastrado. Busca os dois lados antes --
    // assim um id inexistente vira 404 com mensagem clara, em vez de o
    // PostgreSQL recusar a chave estrangeira (e virar 500).
    public TarefaResponseDTO vincularResponsavel(Long tarefaId, Long responsavelId) {
        // Busca a tarefa ou lança TarefaNaoEncontradaException (404).
        Tarefa tarefa = buscarEntidade(tarefaId);
        // Troca o vínculo (se já existia um, é substituído). O id vem validado
        // pelo @NotNull do VinculoResponsavelDTO; inexistente = 404.
        tarefa.setResponsavelVinculado(buscarResponsavel(responsavelId));
        // O Hibernate gera UPDATE tarefas SET responsavel_id = ? WHERE id = ?
        return mapper.toResponse(repository.save(tarefa));
    }

    // Remove o vínculo -- responsavel_id volta a ser NULL.
    public void desvincularResponsavel(Long tarefaId) {
        // Busca a tarefa ou lança TarefaNaoEncontradaException (404).
        Tarefa tarefa = buscarEntidade(tarefaId);
        // null na entidade vira NULL na coluna responsavel_id.
        tarefa.setResponsavelVinculado(null);
        // Persiste a alteração.
        repository.save(tarefa);
    }

    // Busca INTERNA: devolve a entidade, para os métodos acima alterarem e
    // salvarem. É private -- entidade não sai deste Service.
    private Tarefa buscarEntidade(Long id) {
        // Consulta o banco e transforma ausência em exceção de negócio (404).
        return repository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    // Transforma o responsavelId do DTO em entidade. É aqui, no Service, que
    // um id vira objeto: o mapper não consulta o banco. Id null = sem vínculo
    // (devolve null); id inexistente = ResponsavelNaoEncontradoException (404),
    // em vez de o PostgreSQL recusar a chave estrangeira (e virar 500).
    private Responsavel buscarResponsavel(Long responsavelId) {
        // O campo é opcional no DTO: nada a buscar.
        if (responsavelId == null) {
            return null;
        }
        // Busca o responsável ou lança a exceção que o ApiExceptionHandler transforma em 404.
        return responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(responsavelId));
    }
}
