# 🧩 Projeto Backend com Arquitetura de Microsserviços

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![AWS](https://img.shields.io/badge/Amazon_AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

## 📖 Sobre o Projeto

Este é um projeto backend desenvolvido para aplicar e demonstrar conceitos avançados de engenharia de software, com foco principal em **arquitetura de microsserviços**, **modularização** e uma robusta estratégia de **testes unitários e de integração**.

A aplicação foi construída de forma colaborativa, com cada componente desenvolvido de maneira independente e posteriormente integrado aos demais. O processo foi guiado por **requisitos de negócio**, **casos de uso detalhados** e **contratos de API definidos com Swagger**.

---

## 🏛️ Arquitetura e Processo de Desenvolvimento

A arquitetura do sistema foi baseada em microsserviços desacoplados, com responsabilidades bem definidas. Os principais pilares do desenvolvimento foram:

- **🔧 Desenvolvimento Individual:**  
  Cada microsserviço foi implementado de forma isolada, garantindo coesão e autonomia.

- **📜 Definição de Contratos:**  
  A comunicação entre serviços foi padronizada com **Swagger (OpenAPI)**, garantindo interoperabilidade e clareza nas interfaces.

- **🔁 Integração Real em Ambiente na AWS:**  
  Os microsserviços foram **implantados na AWS**, e a **comunicação real entre os serviços foi feita por meio das URLs públicas** hospedadas na nuvem. A troca de mensagens foi validada em produção, com os serviços se integrando de fato por meio de chamadas HTTP reais para endpoints distribuídos. Além dos testes automatizados, a integração foi testada funcionalmente via execução real em ambiente remoto.

- **✅ Qualidade e Deploy:**  
  A qualidade do código foi monitorada com **SonarQube**, e o deploy automatizado via **CI/CD** na **AWS**.

---

## 👥 Equipe e Contribuições

| Contribuição                        | Membro                   | GitHub / GitLab                                               |
|------------------------------------|--------------------------|----------------------------------------------------------------|
| Microsserviço de Equipamentos      | Maria Clara Barboza      | [GitLab - Equipamento](https://gitlab.com/mariaclara26-group/equipamento.git) |
| Microsserviço de Serviços Externos | Bernardo Mansano         | [GitLab - API Externo](https://gitlab.com/unirio4/sistema-de-controle-de-bicicletario/api-externo) |
| Code Review, Pipeline & Deploy     | João Victor Campbell     | [GitHub - João Campbell](https://github.com/joaocampbell2)     |

---

## 🛠️ Tecnologias Utilizadas

### 💻 Linguagem e Framework
- **Java:** Linguagem principal do projeto.
- **Spring Boot:** Framework para criação de APIs e microsserviços.

### ☁️ Infraestrutura e Deploy
- **Amazon Web Services (AWS):** Plataforma de nuvem utilizada para hospedagem da aplicação.

### ✅ Qualidade e Documentação
- **SonarQube:** Análise contínua de qualidade de código.
- **Swagger (OpenAPI):** Definição e documentação das APIs REST.

### 🧪 Testes
- **JUnit 5:** Framework para testes unitários.
- **Mockito:** Ferramenta de mocking para testes automatizados.
