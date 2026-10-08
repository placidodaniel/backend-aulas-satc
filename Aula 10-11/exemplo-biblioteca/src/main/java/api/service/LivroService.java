// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a lista devolvida na listagem.
import java.util.List;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa os DTOs de entrada e de saída do livro.
import api.dto.LivroRequestDTO;
import api.dto.LivroResponseDTO;
// Importa o mapper que traduz DTO <-> entidade.
import api.mapper.LivroMapper;
// Importa a entidade persistida.
import api.model.Livro;
// Importa o repositório usado pelo service.
import api.repository.LivroRepository;

// CAMADA: Service
// RESPONSABILIDADE: cadastrar, listar e buscar livros. Recebe DTO, usa o mapper
//                   e devolve DTO: a entidade Livro não sobe para o Controller.
// REGRAS: nenhuma nesta etapa. A disponibilidade do livro muda com as regras
//         R2 e R3, que moram no EmprestimoService.
// ROTAS ATENDIDAS: POST /livros, GET /livros e GET /livros/{id}.
// SITUAÇÃO: implementada.
// Registra a classe como bean da camada de serviço.
@Service
public class LivroService {

    // Acesso à tabela livros.
    private final LivroRepository repository;

    // Converte DTO <-> entidade.
    private final LivroMapper mapper;

    // Recebe as dependências que o Spring injeta automaticamente.
    public LivroService(LivroRepository repository, LivroMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.mapper = mapper;
    }

    // POST /livros: monta a entidade, grava e devolve o livro com o id gerado.
    public LivroResponseDTO criar(LivroRequestDTO dto) {
        // O mapper monta o livro novo, ainda sem id.
        Livro livro = mapper.toEntity(dto);
        // O banco gera o id no INSERT e devolve a entidade preenchida.
        Livro salvo = repository.save(livro);
        // A entidade para aqui: para cima vai só o DTO.
        return mapper.toResponse(salvo);
    }

    // GET /livros: devolve todos os livros. Sem livros, a lista vem vazia.
    public List<LivroResponseDTO> listarTodos() {
        // Busca todos e converte a lista inteira.
        return mapper.toResponseList(repository.findAll());
    }

    // GET /livros/{id}: devolve um livro ou lança a exceção que vira 404.
    public LivroResponseDTO buscarPorId(Long id) {
        // findById devolve um Optional; se estiver vazio, lança a exceção em vez de devolver null.
        Livro livro = repository.findById(id).orElseThrow(() -> new LivroNaoEncontradoException(id));
        // Converte o livro encontrado.
        return mapper.toResponse(livro);
    }
}
