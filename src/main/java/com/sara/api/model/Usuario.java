package com.sara.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 8)
    private String cep;

    private String endereco;

    private String numero;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 14)
    private String cpfCnpj;

    @Column(nullable = false)
    private String email;

    private String token;

    @Column(name = "data_expiracao")
    private java.time.LocalDateTime dataExpiracao;

    @Column(length = 15)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String bairro = "A designar";

    private String observacao;

    private String paroco;

    private String secretario;

    private String tesoureiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pagamento_id")
    private FormaPagamento formaPagamento;

    private java.math.BigDecimal desconto;

    @Column(name = "modalidade_entrega")
    private String modalidadeEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_frete_id")
    private TabelaFrete tabelaFrete;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento_autorizado", nullable = false)
    private MetodoPagamentoAutorizado metodoPagamentoAutorizado = MetodoPagamentoAutorizado.APENAS_NA_ENTREGA;

    @Column(nullable = false, length = 32)
    private String senha;

    private Boolean ativo = true;

    @Column(name = "permitir_parcelamento", nullable = false)
    private Boolean permitirParcelamento = false;

    @Column(name = "ativar_desconto_a_vista", nullable = false)
    private Boolean ativarDescontoAVista = false;

    @ManyToMany
    @JoinTable(
        name = "usuario_opcao_parcelamento",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "opcao_parcelamento_id")
    )
    private List<OpcaoParcelamento> opcoesParcelamento = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.cpfCnpj;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo != null ? this.ativo : false;
    }
}
