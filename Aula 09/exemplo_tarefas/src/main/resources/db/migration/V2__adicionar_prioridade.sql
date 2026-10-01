-- Aula 08 (Exercício 5): acrescenta a prioridade (1 a 5) em uma tabela que JÁ EXISTE.
-- A V1 não é editada -- o Flyway já registrou o checksum dela em
-- flyway_schema_history; editar a V1 faria o startup falhar na validação.
-- DEFAULT 3 preenche as linhas antigas (as tarefas de exemplo da V1), senão
-- o NOT NULL não poderia ser aplicado a elas.
ALTER TABLE tarefas ADD COLUMN prioridade INTEGER NOT NULL DEFAULT 3;
