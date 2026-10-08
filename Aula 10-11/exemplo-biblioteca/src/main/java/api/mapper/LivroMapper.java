// Define o pacote dos mappers.
package api.mapper;

// Importa a lista usada na conversão em lote.
import java.util.List;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa os DTOs de entrada e de saída do livro.
import api.dto.LivroRequestDTO;
import api.dto.LivroResponseDTO;
// Importa a entidade convertida.
import api.model.Livro;

// CAMADA: Mapper
// RESPONSABILIDADE: converter LivroRequestDTO em Livro, e Livro em LivroResponseDTO.
// SITUAÇÃO: implementada. Tem só os métodos que a API simples usa; o
//           updateEntity, do PUT, entra na próxima etapa.
// @Component: o Spring cria um LivroMapper e injeta no LivroService.
@Component
public class LivroMapper {

    // POST: monta um livro novo a partir do DTO. id e disponivel não vêm do
    // cliente: o banco gera o id e todo exemplar novo nasce disponível.
    public Livro toEntity(LivroRequestDTO dto) {
        // Usa o construtor de cadastro da entidade.
        return new Livro(dto.titulo(), dto.isbn());
    }

    // Toda resposta: copia da entidade só o que o contrato da API mostra.
    public LivroResponseDTO toResponse(Livro livro) {
        // Monta o record com os quatro campos de saída.
        return new LivroResponseDTO(livro.getId(), livro.getTitulo(), livro.getIsbn(), livro.isDisponivel());
    }

    // GET de listagem: converte cada livro da lista.
    public List<LivroResponseDTO> toResponseList(List<Livro> livros) {
        // Reaproveita o toResponse para cada item.
        return livros.stream().map(this::toResponse).toList();
    }
}
