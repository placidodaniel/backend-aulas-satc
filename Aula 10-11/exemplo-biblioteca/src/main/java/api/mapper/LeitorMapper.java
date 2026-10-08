// Define o pacote dos mappers.
package api.mapper;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// CAMADA: Mapper
// RESPONSABILIDADE: converter LeitorRequestDTO em Leitor, e Leitor em LeitorResponseDTO.
//                   Também vai ser usado pelo EmprestimoMapper, para montar o
//                   leitor que aparece dentro de cada empréstimo.
// SITUAÇÃO: montada. Implementação na próxima etapa (toEntity, toResponse e toResponseList).
@Component
public class LeitorMapper {
}
