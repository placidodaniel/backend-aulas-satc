// Mesmo pacote da classe testada.
package api.mcp;

// Importa as asserções do AssertJ.
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
// Importa os utilitários do Mockito.
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
import api.service.TarefaService;

// Aula 14: teste UNITÁRIO das ferramentas MCP.
// O TarefaService é um mock: aqui só interessa a TRADUÇÃO que a ferramenta faz
// (filtros, conversão da data, validação, "concluir" que não reabre).
// O validador é o de verdade, igual ao que o Spring injeta.
class TarefaMcpToolsTest {

    private TarefaService service;
    private TarefaMcpTools tools;

    @BeforeEach
    void preparar() {
        service = mock(TarefaService.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        tools = new TarefaMcpTools(service, validator);
    }

    // Monta um DTO de resposta curto para os testes.
    private TarefaResponseDTO tarefa(long id, boolean concluida, boolean atrasada) {
        return new TarefaResponseDTO(id, "Tarefa " + id, concluida, "Ana", LocalDate.now().plusDays(1),
                LocalDate.now(), 3, 1, atrasada, null);
    }

    @Test
    @DisplayName("listar_tarefas: o filtro 'abertas' tira as concluídas")
    void listarAbertas() {
        when(service.listarTodas()).thenReturn(List.of(tarefa(1, false, false), tarefa(2, true, false)));

        assertThat(tools.listarTarefas("abertas", null)).extracting(TarefaResponseDTO::id).containsExactly(1L);
    }

    @Test
    @DisplayName("listar_tarefas: com responsável, usa a busca por responsável")
    void listarPorResponsavel() {
        when(service.buscarPorResponsavel("Ana")).thenReturn(List.of(tarefa(1, false, true)));

        assertThat(tools.listarTarefas("atrasadas", " Ana ")).hasSize(1);
        verify(service, never()).listarTodas();
    }

    @Test
    @DisplayName("listar_tarefas: situação desconhecida vira erro com a lista do que vale")
    void listarSituacaoInvalida() {
        when(service.listarTodas()).thenReturn(List.of());

        assertThatThrownBy(() -> tools.listarTarefas("pendentes", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("todas, abertas, concluidas ou atrasadas");
    }

    @Test
    @DisplayName("cadastrar_tarefa: dados válidos chegam ao Service no mesmo DTO do REST")
    void cadastrarValido() {
        when(service.criar(any())).thenReturn(tarefa(10, false, false));

        TarefaResponseDTO criada = tools.cadastrarTarefa("Estudar MCP", "Ana", LocalDate.now().plusDays(2).toString(), 4);

        assertThat(criada.id()).isEqualTo(10L);
        verify(service).criar(new TarefaRequestDTO("Estudar MCP", "Ana", LocalDate.now().plusDays(2), 4, null));
    }

    @Test
    @DisplayName("cadastrar_tarefa: data em outro formato explica o formato certo e não salva")
    void cadastrarDataEmOutroFormato() {
        assertThatThrownBy(() -> tools.cadastrarTarefa("Estudar", "Ana", "01/12/2026", 3))
                .hasMessageContaining("yyyy-MM-dd");
        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("cadastrar_tarefa: a validação do DTO roda também na porta MCP")
    void cadastrarInvalido() {
        assertThatThrownBy(() -> tools.cadastrarTarefa(" ", "Ana", LocalDate.now().minusDays(1).toString(), 9))
                .hasMessageContaining("titulo")
                .hasMessageContaining("dataPrazo")
                .hasMessageContaining("prioridade");
        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("concluir_tarefa: já concluída não é reaberta (idempotente)")
    void concluirJaConcluida() {
        when(service.buscarPorId(5L)).thenReturn(tarefa(5, true, false));

        assertThat(tools.concluirTarefa(5L).concluida()).isTrue();
        verify(service, never()).alternarConcluida(5L);
    }

    @Test
    @DisplayName("concluir_tarefa: aberta é concluída")
    void concluirAberta() {
        when(service.buscarPorId(6L)).thenReturn(tarefa(6, false, false));
        when(service.alternarConcluida(6L)).thenReturn(tarefa(6, true, false));

        assertThat(tools.concluirTarefa(6L).concluida()).isTrue();
    }
}
