CREATE TABLE configuracao (
    id SERIAL PRIMARY KEY,
    mail_host VARCHAR(255),
    mail_port INTEGER,
    mail_username VARCHAR(255),
    mail_password VARCHAR(255),
    mail_auth BOOLEAN DEFAULT TRUE,
    mail_starttls BOOLEAN DEFAULT TRUE,
    emails_notificacao TEXT,
    email_ativo BOOLEAN DEFAULT FALSE
);

INSERT INTO configuracao (mail_host, mail_port, mail_username, mail_password, mail_auth, mail_starttls, emails_notificacao, email_ativo)
VALUES ('smtp.gmail.com', 587, '', '', TRUE, TRUE, '', FALSE);
