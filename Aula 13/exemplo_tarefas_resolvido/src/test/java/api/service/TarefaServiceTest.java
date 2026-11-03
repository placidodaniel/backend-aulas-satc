package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
import api.mapper.ResponsavelMapper;
import api.mapper.TarefaMapper;
import api.messaging.TarefaEventoPublisher;
import api.model.Tarefa;
import api.repository.ResponsavelRepository;
import api.repository.TarefaRepository;

// Teste unitário do TarefaService: repositórios e publisher são mocks; o mapper é o de verdade
// (é uma classe pura, sem dependência externa, então não vale a pena fingir).
class TarefaServiceTest {

    private TarefaRepository repository;
    private ResponsavelRepository responsavelRepository;
    private TarefaEventoPublisher eventos;
    private PrevisaoService previsaoService;
    private TarefaService service;

    @BeforeEach
    void preparar() {
        repository = mock(TarefaRepository.class);
        responsavelRepository = mock(ResponsavelRepository.class);
        eventos = mock(TarefaEventoPublisher.class);
        previsaoService = mock(PrevisaoService.class);
        service = new TarefaService(repository, responsavelRepository, new TarefaMapper(new ResponsavelMapper()),
                previsaoService, eventos);
    }

    private Tarefa tarefa(long id) {
        return new Tarefa(id, "Estudar testes", "Ana", LocalDate.now().plusDays(3), 3);
    }

    @Test
    @DisplayName("criar salva a tarefa e publica o evento 'criada'")
    void criarPublicaEvento() {
        when(repository.save(any(Tarefa.class))).thenAnswer(inv -> tarefa(7));

        TarefaResponseDTO criada = service.criar(
                new TarefaRequestDTO("Estudar testes", "Ana", LocalDate.now().plusDays(3), 3, null));

        assertThat(criada.id()).isEqualTo(7L);
        assertThat(criada.concluida()).isFalse();
        verify(eventos).publicar(eq("criada"), any(Tarefa.class));
    }

    @Test
    @DisplayName("criar com responsavelId inexistente: ResponsavelNaoEncontradoException e nada é salvo")
    void criarComResponsavelInexistente() {
        when(responsavelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(
                new TarefaRequestDTO("X", "Ana", LocalDate.now(), 1, 99L)))
                .isInstanceOf(ResponsavelNaoEncontradoException.class);
        verify(repository, never()).save(any());
        verify(eventos, never()).publicar(any(), any(Tarefa.class));
    }

    @Test
    @DisplayName("alternar conclusão: pendente vira concluída e publica 'concluida'")
    void alternarConclui() {
        Tarefa t = tarefa(1);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        assertThat(service.alternarConcluida(1L).concluida()).isTrue();
        verify(eventos).publicar("concluida", t);
    }

    @Test
    @DisplayName("alternar de novo reabre e publica 'reaberta'")
    void alternarReabre() {
        Tarefa t = tarefa(1);
        t.setConcluida(true);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        assertThat(service.alternarConcluida(1L).concluida()).isFalse();
        verify(eventos).publicar("reaberta", t);
    }

    @Test
    @DisplayName("buscar id inexistente: TarefaNaoEncontradaException")
    void buscarInexistente() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(5L)).isInstanceOf(TarefaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("remover id inexistente: não apaga e não publica")
    void removerInexistente() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(5L)).isInstanceOf(TarefaNaoEncontradaException.class);
        verify(repository, never()).deleteById(any());
        verify(eventos, never()).publicar(any(), any(), any());
    }

    @Test
    @DisplayName("remover publica 'removida' com o título da tarefa apagada")
    void removerPublica() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa(1)));

        service.remover(1L);

        verify(repository).deleteById(1L);
        verify(eventos).publicar("removida", 1L, "Estudar testes");
    }

    @Test
    @DisplayName("prazo já passado: devolve a tarefa com aviso, sem chamar a previsão")
    void prazoPassadoGeraAviso() {
        Tarefa t = new Tarefa(1L, "Velha", "Ana", LocalDate.now().minusDays(2), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(previsaoService.temPrevisao(t.getDataPrazo())).thenReturn(false);

        var resultado = service.buscarComPrevisao(1L, -28.6, -49.3);

        assertThat(resultado.previsao()).isNull();
        assertThat(resultado.aviso()).contains("já passou");
        verify(previsaoService, never()).buscar(org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.anyDouble(), any());
    }
}
