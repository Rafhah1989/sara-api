package com.sara.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuracao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mail_host")
    private String mailHost;

    @Column(name = "mail_port")
    private Integer mailPort;

    @Column(name = "mail_username")
    private String mailUsername;

    @Column(name = "mail_password")
    private String mailPassword;

    @Column(name = "mail_auth")
    private Boolean mailAuth = true;

    @Column(name = "mail_starttls")
    private Boolean mailStarttls = true;

    @Column(name = "emails_notificacao", columnDefinition = "TEXT")
    private String emailsNotificacao;

    @Column(name = "email_ativo")
    private Boolean emailAtivo = false;
}
