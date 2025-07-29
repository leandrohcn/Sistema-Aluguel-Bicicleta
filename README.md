Projeto Backend com Arquitetura de Microsserviços

https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot">
https://img.shields.io/badge/Amazon_AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white" alt="AWS">
https://img.shields.io/badge/SonarQube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white" alt="SonarQube">
https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger">

📖 Sobre o Projeto
Este é um projeto backend desenvolvido para aplicar e demonstrar conceitos avançados de engenharia de software, com foco principal em arquitetura de microsserviços, modularização e uma robusta estratégia de testes unitários e de integração.

A aplicação foi construída de forma colaborativa, seguindo um fluxo de trabalho onde cada componente foi desenvolvido de maneira independente e, posteriormente, conectado aos demais. Todo o processo de integração foi guiado pelos requisitos de negócio, casos de uso detalhados e os contratos de API definidos com o Swagger.

Architecture & Development Process
O projeto foi dividido em módulos independentes, cada um representando um microsserviço com uma responsabilidade única. A abordagem foi a seguinte:

Desenvolvimento Individual: Cada membro da equipe implementou seu microsserviço de forma isolada, garantindo a coesão e a autonomia do componente.

Definição de Contratos: A comunicação entre os serviços foi padronizada utilizando o Swagger (OpenAPI), que serviu como uma fonte única de verdade para as interfaces de API.

Integração e Testes: Com os microsserviços desenvolvidos, realizamos a integração, validando a comunicação e o fluxo de dados entre eles através de testes de integração completos.

Qualidade e Deploy: A qualidade do código foi continuamente monitorada com o SonarQube, e a manutenção do pipeline de CI/CD garantiu a automação do deploy da aplicação na AWS.

👥 Equipe e Contribuições
Este projeto é o resultado da colaboração de uma equipe dedicada, onde cada membro teve um papel fundamental para o sucesso da aplicação.

Contribuição	Membro	GitHub
Microsserviço de Equipamentos	Maria Clara Barboza	
Microsserviço de Serviços Externos	Matheus Mansano	
Code Review, Pipeline & Deploy	João Victor Campbell	

Exportar para as Planilhas
🛠️ Tecnologias Utilizadas
Linguagem e Framework:

Java: Linguagem principal para o desenvolvimento.

Spring Boot: Framework para a criação dos microsserviços, facilitando a configuração e o desenvolvimento.

Infraestrutura e Deploy:

Amazon Web Services (AWS): Plataforma de nuvem utilizada para hospedar e executar a aplicação.

Qualidade e Documentação:

SonarQube: Ferramenta para inspeção contínua da qualidade do código.

Swagger (OpenAPI): Utilizado para projetar, construir, documentar e consumir as APIs RESTful.

Testes:

JUnit 5: Framework para a implementação de testes unitários.

Mockito: Utilizado para criar objetos mock em testes unitários.
