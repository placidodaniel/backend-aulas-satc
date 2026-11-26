// Define o pacote dos repositórios.
package api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import api.model.Usuario;

// Aula 15: acesso à tabela usuarios. O Spring Data gera o SQL pelo nome do método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // SELECT ... WHERE email = ?  (parâmetro, nunca texto colado: sem SQL injection)
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
