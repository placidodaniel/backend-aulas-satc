// "package tributaveis;" TEM que ser a primeira linha de código do arquivo (só comentários antes).
// Regra de ouro: o nome do pacote espelha as pastas -> este arquivo mora em src/main/java/tributaveis/.
// Nome completo (fully qualified name) desta interface: tributaveis.Tributavel
//
// Tributavel ganhou pacote próprio de propósito: ela é o CONTRATO, não pertence nem às
// contas nem aos investimentos. Assim a seta de dependência aponta dos dois para cá
// (contas -> tributaveis <- investimentos), e nenhum pacote de domínio depende do outro.
//
// Declara o pacote; a pasta tributaveis/ precisa existir e ter exatamente este nome.
package tributaveis;

// >>> INTERFACE: só define o "contrato" calcularImposto(), sem implementação.
// Quem implementa (contas.Conta e investimentos.Acao, cada um no seu pacote) é obrigado
// a fornecer o próprio código do método -- e cada um calcula de um jeito diferente.
//
// >>> POLIMORFISMO: é daqui que ele nasce. Duas classes SEM NENHUM PARENTESCO assinam o
// mesmo contrato, então cabem na mesma lista e respondem à mesma chamada de jeitos diferentes.
// Interface não é herança: ninguém aqui é "pai" de ninguém.
//
// O "public" da interface é ESSENCIAL agora que existem pacotes: sem ele, Tributavel
// só seria enxergada dentro do pacote tributaveis, e ninguém de fora conseguiria implementá-la.
//
// Escrevemos "interface", e não "class": aqui não existe atributo nem corpo de método.
public interface Tributavel {

    // Assinatura sem corpo: termina em ";" e quem implementa é obrigado a preencher.
    public double calcularImposto();

// Fim da interface Tributavel.
}
