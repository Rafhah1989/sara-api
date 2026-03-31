package com.sara.api.service;

import com.sara.api.model.ConfiguracaoBoleto;
import com.sara.api.repository.ConfiguracaoBoletoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoBoletoService {

    @Autowired
    private ConfiguracaoBoletoRepository repository;

    @Transactional(readOnly = true)
    public ConfiguracaoBoleto getConfiguracao() {
        return repository.findById(1L).orElseGet(() -> {
            ConfiguracaoBoleto nova = new ConfiguracaoBoleto();
            return repository.save(nova);
        });
    }

    @Transactional
    public ConfiguracaoBoleto salvar(ConfiguracaoBoleto config) {
        config.setId(1L); // Garante o ID do Singleton
        return repository.save(config);
    }
}
