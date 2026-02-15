package com.sara.api.validator;

import com.sara.api.dto.UsuarioRequestDTO;
import com.sara.api.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class UsuarioValidator {

    public void validar(UsuarioRequestDTO request) {
        validarNome(request.getNome());
        validarSenha(request.getSenha());
        validarCep(request.getCep());
        validarCpfCnpj(request.getCpfCnpj());
        validarTelefone(request.getTelefone());
        validarCidade(request.getCidade());
        validarUf(request.getUf());
    }

    private void validarNome(String nome) {
        if (nome == null || nome.length() > 100) {
            throw new ValidationException("O campo nome deve ter no máximo 100 caracteres");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || !senha.matches("\\d{6}")) {
            throw new ValidationException("A senha deve conter seis números");
        }
    }

    private void validarCep(String cep) {
        if (cep == null || !cep.matches("\\d{8}")) {
            throw new ValidationException("O CEP deve conter exatos oito caracteres numéricos");
        }
    }

    private void validarTelefone(String telefone) {
        if (telefone == null || !telefone.matches("\\(\\d{2}\\) \\d{5}-\\d{4}")) {
            throw new ValidationException("O telefone deve estar no formato (xx) xxxxx-xxxx");
        }
    }

    private void validarCidade(String cidade) {
        if (cidade == null || cidade.isEmpty()) {
            throw new ValidationException("O campo cidade é obrigatório");
        }
        if (cidade.length() > 100) {
            throw new ValidationException("O campo cidade deve ter no máximo 100 caracteres");
        }
    }

    private void validarUf(String uf) {
        if (uf == null || uf.length() != 2) {
            throw new ValidationException("O campo UF deve ter exatos dois caracteres");
        }
    }

    private void validarCpfCnpj(String value) {
        if (value == null) {
            throw new ValidationException("CPF ou CNPJ deve ser informado");
        }

        String cleanValue = value.replaceAll("\\D", "");
        if (cleanValue.length() == 11) {
            if (!isValidCPF(cleanValue)) {
                throw new ValidationException("CPF inválido");
            }
        } else if (cleanValue.length() == 14) {
            if (!isValidCNPJ(cleanValue)) {
                throw new ValidationException("CNPJ inválido");
            }
        } else {
            throw new ValidationException("CPF ou CNPJ deve ter 11 ou 14 dígitos numéricos");
        }
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.matches("(\\d)\\1{10}"))
            return false;

        int sum = 0;
        for (int i = 0; i < 9; i++)
            sum += (10 - i) * (cpf.charAt(i) - '0');
        int digit1 = 11 - (sum % 11);
        if (digit1 > 9)
            digit1 = 0;

        sum = 0;
        for (int i = 0; i < 10; i++)
            sum += (11 - i) * (cpf.charAt(i) - '0');
        int digit2 = 11 - (sum % 11);
        if (digit2 > 9)
            digit2 = 0;

        return (cpf.charAt(9) - '0' == digit1) && (cpf.charAt(10) - '0' == digit2);
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}"))
            return false;

        int[] weights1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int sum = 0;
        for (int i = 0; i < 12; i++)
            sum += (cnpj.charAt(i) - '0') * weights1[i];
        int digit1 = 11 - (sum % 11);
        if (digit1 > 9)
            digit1 = 0;

        int[] weights2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        sum = 0;
        for (int i = 0; i < 13; i++)
            sum += (cnpj.charAt(i) - '0') * weights2[i];
        int digit2 = 11 - (sum % 11);
        if (digit2 > 9)
            digit2 = 0;

        return (cnpj.charAt(12) - '0' == digit1) && (cnpj.charAt(13) - '0' == digit2);
    }
}
