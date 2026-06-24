package com.javanauta.bffagendadortarefas.business;

import com.javanauta.bffagendadortarefas.business.dto.in.LoginRequestDTO;
import com.javanauta.bffagendadortarefas.business.dto.out.TarefasDTOResponse;
import com.javanauta.bffagendadortarefas.business.enums.StatusNotificacaoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // anotação pra log
public class CronService {

    private final TarefasService tarefasService;
    private final EmailService emailService;
    private final UsuarioService usuarioService;

    @Value("${usuario.email}")
    private String email;

    @Value("${usuario.senha}")
    private String senha;

    // cron é sempre sem parametro
    @Scheduled(cron = "${cron.horario}")
    public void buscaTarefasProximaHora() {
        String token = login(converterParaRequestDTO());
        log.info("Iniciada a busca de tarefas");
        LocalDateTime horaAtual = LocalDateTime.now();
        LocalDateTime horaFutura = LocalDateTime.now().plusHours(1);
        List<TarefasDTOResponse> listaTarefas = tarefasService.buscaTarefasAgendadasPorPeriodo(horaAtual, horaFutura, token);
        log.info("Tarefas encontradas " + listaTarefas);

        // pra cada uma das tarefas, será enviado um email e alterado seu status para notificado
        listaTarefas.forEach(tarefa -> {
            emailService.enviaEmail(tarefa);
            log.info("Email enviado para o usuario " + tarefa.getEmailUsuario());
            tarefasService.alteraStatus(StatusNotificacaoEnum.NOTIFICADO, tarefa.getId(),
                    token);
        });
        log.info("Finalizada a busca e notificação de tarefas");
    }

    public String login(LoginRequestDTO dto) {
        return usuarioService.loginUsuario(dto);
    }

    public LoginRequestDTO converterParaRequestDTO() {
        return LoginRequestDTO.builder()
                .email(email)
                .senha(senha)
                .build();
    }
}
