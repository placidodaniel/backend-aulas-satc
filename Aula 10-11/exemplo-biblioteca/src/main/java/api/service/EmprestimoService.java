// Define o pacote da camada de regras de negócio.
package api.service;

// Importa o tipo de data usado nas regras.
import java.time.LocalDate;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;
// Importa a anotação que faz o método inteiro valer como uma operação só no banco.
import org.springframework.transaction.annotation.Transactional;

// Importa os DTOs de entrada e de saída do empréstimo.
import api.dto.EmprestimoRequestDTO;
import api.dto.EmprestimoResponseDTO;
// Importa o mapper que monta a resposta.
import api.mapper.EmprestimoMapper;
// Importa as entidades envolvidas nas regras.
import api.model.Emprestimo;
import api.model.Leitor;
import api.model.Livro;
// Importa os repositórios usados pelas regras.
import api.repository.EmprestimoRepository;
import api.repository.LeitorRepository;
import api.repository.LivroRepository;

// CAMADA: Service
// RESPONSABILIDADE: regras de negócio dos empréstimos.
// REGRAS: R1 (no máximo 3 empréstimos em aberto por leitor),
//         R2 (livro emprestado não pode ser emprestado de novo) e
//         R3 (prazo de devolução de 14 dias).
// ROTAS ATENDIDAS: POST /emprestimos e PUT /emprestimos/{id}/devolver.
// SITUAÇÃO: implementada.
@Service
public class EmprestimoService {

    // R1: quantos empréstimos em aberto um leitor pode ter ao mesmo tempo.
    private static final int MAXIMO_EM_ABERTO = 3;

    // R3: quantos dias o leitor tem para devolver o livro.
    private static final int PRAZO_DEVOLUCAO_DIAS = 14;

    // Acesso à tabela emprestimos.
    private final EmprestimoRepository repository;

    // Usado para buscar o leitor pelo id que chegou no DTO.
    private final LeitorRepository leitorRepository;

    // Usado para buscar o livro pelo id que chegou no DTO.
    private final LivroRepository livroRepository;

    // Monta a resposta, com leitor e livro completos.
    private final EmprestimoMapper mapper;

    // Recebe as dependências que o Spring injeta automaticamente.
    public EmprestimoService(EmprestimoRepository repository, LeitorRepository leitorRepository,
                             LivroRepository livroRepository, EmprestimoMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.leitorRepository = leitorRepository;
        this.livroRepository = livroRepository;
        this.mapper = mapper;
    }

    // POST /emprestimos. @Transactional: gravar o empréstimo e tirar o livro da
    // estante acontecem juntos; se uma parte falhar, nenhuma fica gravada.
    @Transactional
    public EmprestimoResponseDTO criar(EmprestimoRequestDTO dto) {
        // Entra um id, o Service busca a entidade: 404 se o leitor não existir.
        Leitor leitor = leitorRepository.findById(dto.leitorId())
                .orElseThrow(() -> new LeitorNaoEncontradoException(dto.leitorId()));
        // O mesmo para o livro.
        Livro livro = livroRepository.findById(dto.livroId())
                .orElseThrow(() -> new LivroNaoEncontradoException(dto.livroId()));

        // R1: o leitor não pode passar de 3 empréstimos em aberto.
        if (repository.countByLeitorIdAndDataDevolucaoIsNull(leitor.getId()) >= MAXIMO_EM_ABERTO) {
            // A mensagem vai para o cliente, dentro do ErroDTO, com status 400.
            throw new RegraDeNegocioException("R1: o leitor já tem " + MAXIMO_EM_ABERTO + " empréstimos em aberto");
        }
        // R2: um livro emprestado não pode sair de novo antes da devolução.
        if (!livro.isDisponivel()) {
            // Mesma coisa: 400 com a regra explicada.
            throw new RegraDeNegocioException("R2: o livro " + livro.getId() + " já está emprestado");
        }

        // O livro sai da estante hoje: a data quem decide é a API, não o cliente.
        livro.emprestar();
        // Grava o empréstimo novo; o banco gera o id.
        Emprestimo salvo = repository.save(new Emprestimo(leitor, livro, LocalDate.now()));
        // Converte já com os campos da regra R3.
        return responder(salvo);
    }

    // PUT /emprestimos/{id}/devolver: registra a volta do livro.
    @Transactional
    public EmprestimoResponseDTO devolver(Long id) {
        // 404 se o empréstimo não existir.
        Emprestimo emprestimo = repository.findById(id)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(id));
        // Um livro não volta duas vezes.
        if (!emprestimo.estaEmAberto()) {
            // 400: o pedido é válido no formato, mas não faz sentido para o sistema.
            throw new RegraDeNegocioException("O empréstimo " + id + " já foi devolvido");
        }
        // Registra o dia da devolução e põe o livro de volta na estante.
        emprestimo.registrarDevolucao(LocalDate.now());
        emprestimo.getLivro().devolver();
        // Dentro do @Transactional, o Hibernate grava as duas mudanças sozinho.
        return responder(emprestimo);
    }

    // R3: calcula a data limite e se o empréstimo passou do prazo, e monta a resposta.
    private EmprestimoResponseDTO responder(Emprestimo emprestimo) {
        // A data limite é a retirada mais 14 dias.
        LocalDate dataLimite = emprestimo.getDataRetirada().plusDays(PRAZO_DEVOLUCAO_DIAS);
        // Compara com o dia da devolução ou, se o livro ainda não voltou, com hoje.
        LocalDate referencia = emprestimo.estaEmAberto() ? LocalDate.now() : emprestimo.getDataDevolucao();
        // Atrasado = a referência passou da data limite.
        boolean atrasado = referencia.isAfter(dataLimite);
        // O mapper só monta o DTO; a regra ficou aqui, no Service.
        return mapper.toResponse(emprestimo, dataLimite, atrasado);
    }
}
