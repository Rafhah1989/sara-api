# Sara API

Backend do sistema SARA (Sistema de Automação de Registros Administrativos), desenvolvido para gerenciar produtos, usuários, setores e tabelas de frete.

## Tecnologias
- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Flyway (Migração de Banco)
- PostgreSQL (Banco de Dados)
- Maven (Gerenciador de Dependências)

## Funcionalidades
- CRUD de Produtos
- Gestão de Usuários com controle de permissões (ADMIN/CLIENTE)
- Gestão de Setores e Vínculo com Tabelas de Frete
- Segurança com criptografia de senhas (MD5)

## Como executar
1. Certifique-se de ter o Java 17 e o Maven instalados.
2. Configure as credenciais do PostgreSQL no arquivo `src/main/resources/application.properties`.
3. Execute o comando:
   ```bash
   mvn spring-boot:run
   ```
4. A API estará disponível em `http://localhost:8080`.
