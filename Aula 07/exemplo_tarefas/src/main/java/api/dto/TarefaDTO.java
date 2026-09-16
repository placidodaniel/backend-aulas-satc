// Este arquivo pertence ao pacote "api.dto" e mora em src/main/java/api/dto/.
// Nome completo desta classe: api.dto.TarefaDTO
package api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// >>> PARA QUE SERVE UM DTO (Data Transfer Object): moldar só o que TRAFEGA
// entre cliente e API -- não é o modelo de domínio (Tarefa), é um contrato de
// entrada/saída separado. Três motivos concretos, vistos aqui mesmo:
//
// 1. Só expõe o que o cliente deve enviar. Este DTO tem "titulo", "responsavel"
//    e "dataPrazo" -- mas não tem "id", "concluida" nem "dataCadastro", porque
//    essas três coisas são decididas pela API (o Repository gera o id e a data
//    de cadastro; toda tarefa nasce com concluida=false). Se o cliente pudesse
//    mandar um id ou uma dataCadastro no corpo, poderia forçar valores que não
//    fazem sentido (um id que já existe, uma data de cadastro no futuro...).
//
// 2. É onde a validação acontece, ANTES da regra de negócio rodar. As
//    anotações estão aqui, não em Tarefa. Combinado com @Valid no
//    TarefaController, o Spring barra um corpo inválido com 400 antes de
//    TarefaService.criar()/atualizar() serem chamados -- o service nunca
//    precisa se preocupar com dado inválido.
//
// 3. Desacopla o contrato da API da estrutura interna. Se um dia Tarefa ganhar
//    mais campos, este DTO continua devolvendo/aceitando só o que foi
//    decidido aqui -- nada vaza por engano só porque o model mudou.
//
// É o item "Usar DTOs em vez de expor entidades" da tabela de boas práticas
// no README da Aula 07 -- esta classe é esse item rodando de verdade.
public class TarefaDTO {

    // ---------------------------------------------------------------- atributos

    // >>> VALIDAÇÃO: @NotBlank rejeita três casos -- null, string vazia ("") e
    // string só com espaços (" ") -- os três contam como "sem título" pra regra.
    // "message" é o texto que volta pro cliente quando a validação falha (visto
    // no 400 Bad Request, dentro de errors[].defaultMessage).
    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    // Mesma anotação, mesma regra -- só que pro nome de quem é responsável pela tarefa.
    @NotBlank(message = "Responsável é obrigatório")
    private String responsavel;

    // >>> VALIDAÇÃO: @NotNull é o equivalente do @NotBlank pra tipos que não são
    // String -- LocalDate não tem "vazio" ou "espaços em branco", só existe ou é
    // null. Por isso aqui é @NotNull, não @NotBlank (que só compila em String).
    @NotNull(message = "Data de prazo é obrigatória")
    private LocalDate dataPrazo;

    // ---------------------------------------------------------------- getters/setters

    // Getter: devolve o valor atual de "titulo". É o que o Spring/Jackson chama
    // quando precisa LER o campo -- por exemplo, na hora de rodar a validação
    // do @NotBlank, ou se este DTO um dia precisar virar JSON de novo.
    public String getTitulo() {
        // Devolve o valor guardado no atributo -- sem nenhuma transformação.
        return titulo;
    }

    // Setter: recebe um valor novo e guarda no atributo "titulo". É o método que
    // o Jackson chama sozinho ao converter o JSON do corpo da requisição
    // (@RequestBody no TarefaController) num objeto TarefaDTO -- uma chamada de
    // setTitulo(...) para cada campo que existir no JSON recebido.
    public void setTitulo(String titulo) {
        // this.titulo = campo da classe; titulo (sem "this.") = parâmetro recebido.
        // Sem o "this.", a atribuição "titulo = titulo" não faria sentido (o
        // parâmetro se atribuiria a si mesmo, e o atributo nunca mudaria).
        this.titulo = titulo;
    // Fim de setTitulo().
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    // LocalDate: o Jackson (com o módulo jackson-datatype-jsr310, que já vem
    // junto do spring-boot-starter-web) converte isso pra/de JSON sozinho, no
    // formato "yyyy-MM-dd" -- o mesmo formato de um <input type="date"> do HTML.
    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

// Fim da classe TarefaDTO.
}
