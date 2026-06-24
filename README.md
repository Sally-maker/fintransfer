# FinTransfer

API de transferências financeiras inspirada no modelo PicPay, desenvolvida com Java 21 e Spring Boot.

## Visão Geral

Sistema que permite cadastro de usuários (comuns e lojistas), gerenciamento de carteiras digitais e transferências entre usuários com autorização inteligente via IA.

### Regras de Negócio

- **Usuário Comum** pode enviar e receber transferências
- **Lojista** apenas recebe transferências
- Validação de saldo antes de cada transferência
- Autorização de transações via API da Anthropic (Claude)
- Estorno solicitado pelo pagador e analisado pela IA
- Notificação ao recebedor após cada transferência

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4 | Framework |
| Spring Security + JWT | Autenticação e autorização |
| Spring Data JPA | Persistência |
| PostgreSQL | Banco de dados |
| Flyway | Versionamento de schema |
| JUnit 5 + Mockito | Testes unitários |
| Testcontainers | Testes de integração |
| Docker + Docker Compose | Containerização |
| GitHub Actions | CI/CD |
| Swagger / OpenAPI | Documentação da API |

## Arquitetura

Modular Monolith organizado por domínio:

```
src/main/java/com/gabriel/fintransfer/
├── auth/              # Autenticação (login, registro)
├── notification/      # Notificações (log-based)
├── shared/            # Configurações, exceções, segurança JWT
├── transaction/       # Transferências, estornos, autorização IA
├── user/              # Cadastro e gestão de usuários
└── wallet/            # Carteiras digitais, saldo, depósitos
```

### Decisões Arquiteturais

- **Modular Monolith** em vez de microserviços — complexidade adequada ao escopo, com separação clara de domínios que facilita migração futura
- **Interface + Impl** nos services — inversão de dependência, facilita testes com mocks e troca de implementação
- **TransactionExecutor separado** — isola a lógica transacional (@Transactional) da orquestração de negócio
- **Optimistic Locking** na Wallet (`@Version`) — previne race conditions em transferências concorrentes
- **SecurityConfig stateless** — sem sessão no servidor, cada request carrega o JWT

## Endpoints

### Auth
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar usuário e receber token |
| POST | `/api/v1/auth/login` | Login e receber token |

### Users
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/users` | Listar todos os usuários |
| GET | `/api/v1/users/{id}` | Buscar usuário por ID |

### Wallets
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/wallets/user/{userId}` | Consultar saldo |
| POST | `/api/v1/wallets/user/{userId}/deposit` | Depositar |

### Transactions
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/transactions/transfer` | Realizar transferência |
| GET | `/api/v1/transactions/user/{userId}` | Histórico de transações |
| POST | `/api/v1/transactions/{id}/refund` | Solicitar estorno (pagador, analisado por IA) |

## Como Executar

### Pré-requisitos
- Java 21
- Docker e Docker Compose

### Subindo o ambiente

```bash
# Subir PostgreSQL e pgAdmin
docker compose up -d postgres pgadmin

# Executar a aplicação
./mvnw spring-boot:run
```

Ou subir tudo via Docker:

```bash
docker compose up -d --build
```

### Acessos
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050 (admin@fintransfer.com / admin)

## Testes

```bash
# Testes unitários e integração (requer Docker para Testcontainers)
./mvnw verify
```

### Cobertura de Testes

- **UserServiceTest** — criação, duplicidade de email/CPF, usuário não encontrado
- **TransactionServiceTest** — transferência, lojista como pagador, saldo insuficiente, transação não autorizada
- **WalletServiceTest** — criação de carteira, depósito, carteira não encontrada
- **AuthControllerIntegrationTest** — registro, login, credenciais inválidas, proteção de endpoints

## Variáveis de Ambiente

| Variável | Descrição | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL do PostgreSQL | `jdbc:postgresql://localhost:5432/fintransfer` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `fintransfer` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `fintransfer` |
| `JWT_SECRET` | Chave secreta para tokens JWT | (dev default) |
| `ANTHROPIC_API_KEY` | Chave da API Anthropic | (vazio) |
