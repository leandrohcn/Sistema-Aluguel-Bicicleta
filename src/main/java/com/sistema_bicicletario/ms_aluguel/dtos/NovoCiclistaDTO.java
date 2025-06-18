    package com.sistema_bicicletario.ms_aluguel.dtos;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
    import jakarta.validation.Valid;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import lombok.Getter;
    import lombok.Setter;

    import java.util.Date;


    @Getter @Setter
    public class NovoCiclistaDTO {

        @JsonProperty(required = true)
        @NotBlank (message = "Nome é obrigatório")
        private String nome;

        @JsonProperty(required = true)
        @NotNull (message = "Data com formato inválido")
        private Date nascimento;

        private String cpf;
        private PassaporteDTO passaporte;
        private Nacionalidade nacionalidade;

        @NotBlank
        @Email
        private String email;

        @JsonProperty(required = true)
        @NotBlank (message = "Falta url do documento")
        private String urlFotoDocumento;

        @JsonProperty(required = true)
        @NotNull(message = "Necessita de uma senha")
        private String senha;

        @JsonProperty(required = true)
        @NotNull(message = "Necessita da confirmação de senha")
        private String confirmaSenha;

        @JsonProperty(required = true)
        @Valid
        @NotNull
        private NovoCartaoDeCreditoDTO meioDePagamento;

        public boolean senhaValida() {
            if (senha == null || confirmaSenha == null) {
                return false;
            }
            return senha.equals(confirmaSenha);
        }

    }
