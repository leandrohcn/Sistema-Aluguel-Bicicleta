DELETE FROM funcionario;
DELETE FROM cartao_de_credito;
DELETE FROM ciclista;
DELETE FROM aluguel;


-- Ciclista 1
INSERT INTO ciclista (id, status, nome, data_nascimento, cpf, nacionalidade, email, senha, url_foto_documento, hora_confirmacao_email, aluguel_ativo, numero_passaporte, validade_passaporte, pais)
VALUES (1, 'ATIVO', 'Fulano Beltrano', '2021-05-02', '78804034009', 'BRASILEIRO', 'user@example.com', 'ABC123', NULL, NULL, false, NULL, NULL, NULL);

-- Ciclista 2
INSERT INTO ciclista (id, status, nome, data_nascimento, cpf, nacionalidade, email, senha, url_foto_documento, hora_confirmacao_email, aluguel_ativo, numero_passaporte, validade_passaporte, pais)
VALUES (2, 'AGUARDANDO_CONFIRMACAO', 'Fulano Beltrano', '2021-05-02', '43943488039', 'BRASILEIRO', 'user2@example.com', 'ABC123', NULL, NULL, false, NULL, NULL, NULL);

-- Ciclista 3
INSERT INTO ciclista (id, status, nome, data_nascimento, cpf, nacionalidade, email, senha, url_foto_documento, hora_confirmacao_email, aluguel_ativo, numero_passaporte, validade_passaporte, pais)
VALUES (3, 'ATIVO', 'Fulano Beltrano', '2021-05-02', '10243164084', 'BRASILEIRO', 'user3@example.com', 'ABC123', NULL, NULL, false, NULL, NULL, NULL);

-- Ciclista 4
INSERT INTO ciclista (id, status, nome, data_nascimento, cpf, nacionalidade, email, senha, url_foto_documento, hora_confirmacao_email, aluguel_ativo, numero_passaporte, validade_passaporte, pais)
VALUES (4, 'ATIVO', 'Fulano Beltrano', '2021-05-02', '30880150017', 'BRASILEIRO', 'user4@example.com', 'ABC123', NULL, NULL, false, NULL, NULL, NULL);

INSERT INTO cartao_de_credito (id, nome_titular, numero, validade, cvv)
VALUES (1, 'Fulano Beltrano', '4242424242424242', '2025-12-31', '132');

-- Cartão do Ciclista 2 (note que o id = 2)
INSERT INTO cartao_de_credito (id, nome_titular, numero, validade, cvv)
VALUES (2, 'Fulano Beltrano', '4242424242424242', '2025-12-31', '132');

-- Cartão do Ciclista 3 (note que o id = 3)
INSERT INTO cartao_de_credito (id, nome_titular, numero, validade, cvv)
VALUES (3, 'Fulano Beltrano', '4242424242424242', '2025-12-31', '132');

-- Cartão do Ciclista 4 (note que o id = 4)
INSERT INTO cartao_de_credito (id, nome_titular, numero, validade, cvv)
VALUES (4, 'Fulano Beltrano', '4242424242424242', '2025-12-31', '132');

INSERT INTO funcionario (matricula,nome, senha, email, idade, cpf, funcao)
VALUES (1,'Beltrano', '123', 'employee@example.com', 25, '99999999999', 'Reparador');

-- Aluguel 1: Início no momento da inserção
INSERT INTO aluguel (id, numero_bicicleta, ciclista, tranca_inicio, cobranca, hora_inicio, hora_fim, tranca_fim, nome_titular, final_cartao, valor_extra)
VALUES (1, 3, 3, 2, 1, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, NULL);

-- Aluguel 2: Início 2 horas antes da inserção
INSERT INTO aluguel (id, numero_bicicleta, ciclista, tranca_inicio, cobranca, hora_inicio, hora_fim, tranca_fim, nome_titular, final_cartao, valor_extra)
VALUES (2, 5, 4, 4, 2, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, NULL, NULL, NULL, NULL, NULL);

-- Aluguel 3: Início 2 horas antes e fim no momento da inserção
INSERT INTO aluguel (id, numero_bicicleta, ciclista, tranca_inicio, cobranca, hora_inicio, hora_fim, tranca_fim, nome_titular, final_cartao, valor_extra)
VALUES (3, 1, 3, 1, 3, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP, 2, NULL, NULL, NULL);


ALTER TABLE ciclista ALTER COLUMN id RESTART WITH 5;
ALTER TABLE cartao_de_credito ALTER COLUMN id RESTART WITH 5;
ALTER TABLE funcionario ALTER COLUMN matricula RESTART WITH 2;
ALTER TABLE aluguel ALTER COLUMN id RESTART WITH 4;