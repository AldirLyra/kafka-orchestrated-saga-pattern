---

# **Arquitetura de Microsserviços: Padrão Saga Orquestrado com Kafka e Spring Boot**

Este repositório contém a implementação do **padrão Saga Orquestrado** utilizando **Apache Kafka** e **Spring Boot**. O projeto faz parte dos estudos realizados no curso da **Udemy** e demonstra uma arquitetura de microsserviços baseada em eventos, garantindo consistência distribuída.

## 🚀 **Tecnologias Utilizadas**
- **Java 17** + **Spring Boot 3**
- **Apache Kafka** + **Redpanda Console**
- **PostgreSQL** + **MongoDB**
- **Docker** + **Docker Compose**
- **Gradle** 

## 🏗️ **Arquitetura do Projeto**
A aplicação segue um modelo **orquestrado de Saga**, onde um **Orchestrator-Service** gerencia os estados e interações entre os microsserviços:

- **Order-Service**: Criação de pedidos e início da saga. (MongoDB)
- **Orchestrator-Service**: Coordena a execução do fluxo da saga. (Sem banco)
- **Product-Validation-Service**: Valida os produtos do pedido. (PostgreSQL)
- **Payment-Service**: Processa o pagamento do pedido. (PostgreSQL)
- **Inventory-Service**: Atualiza o estoque dos produtos. (PostgreSQL)

Todos os serviços são iniciados automaticamente pelo **Docker Compose**.

![Arquitetura](docs/Arquitetura.png)

## ⚙️ **Execução do Projeto**
O projeto pode ser executado de diferentes formas:

### 🔹 **1. Via Docker Compose (Recomendado)**
```sh
docker-compose up --build -d
```
> Esse comando sobe todos os microsserviços e dependências automaticamente.

### 🔹 **2. Via Script Python**
```sh
python build.py
```
> Automatiza a build dos serviços e executa o `docker-compose`.

### 🔹 **3. Executando Somente Bancos e Kafka**
```sh
docker-compose up --build -d order-db kafka product-db payment-db inventory-db
```
> Apenas os bancos de dados e o Apache Kafka serão iniciados.

### 🔹 **4. Execução Manual**
```sh
gradle build -x test
gradle bootRun
```
Ou rode diretamente:
```sh
java -jar build/libs/nome_do_jar.jar
```

## 🌐 **Acessando a Aplicação**
- **Swagger UI** → [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)
- **Redpanda Console** → [http://localhost:8081](http://localhost:8081)

### 📌 **Principais Portas**
| Serviço                     | Porta  |
|-----------------------------|--------|
| Order-Service               | `3000` |
| Orchestrator-Service        | `8080` |
| Product-Validation-Service  | `8090` |
| Payment-Service             | `8091` |
| Inventory-Service           | `8092` |
| Apache Kafka                | `9092` |
| Redpanda Console            | `8081` |
| PostgreSQL (Product-DB)     | `5432` |
| PostgreSQL (Payment-DB)     | `5433` |
| PostgreSQL (Inventory-DB)   | `5434` |
| MongoDB (Order-DB)          | `27017` |

## 📝 **Endpoints da API**
### 🔹 **Criar um Pedido (Inicia a Saga)**
```http
POST http://localhost:3000/api/order
```
**Payload**
```json
{
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.50
      },
      "quantity": 3
    }
  ]
}
```

### 🔹 **Consultar Status da Saga**
```http
GET http://localhost:3000/api/event?orderId={ID_DO_PEDIDO}
```
Ou
```http
GET http://localhost:3000/api/event?transactionId={ID_DA_TRANSACAO}
```

## 🛠️ **Consultas no MongoDB**
Para acessar o **MongoDB** via terminal:
```sh
docker exec -it order-db mongosh "mongodb://admin:123456@localhost:27017"
```
Exemplos de queries:
```sh
show dbs
use admin
show collections
db.order.find()
db.event.find()
```

## 📌 **Sobre o Autor**
👨‍💻 **[Aldir Lyra]**  
🚀 Desenvolvedor Fullstack.
---
