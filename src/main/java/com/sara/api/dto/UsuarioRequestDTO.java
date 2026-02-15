package com.sara.api.dto;

import com.sara.api.model.Role;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nome;
    private String cep;
    private String endereco;
    private String numero;
    private String cidade;
    private String uf;
    private String cpfCnpj;
    private String telefone;
    private Role role;
    private String senha;
}
