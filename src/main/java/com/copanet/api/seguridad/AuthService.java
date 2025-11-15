package com.copanet.api.seguridad;

import com.copanet.api.seguridad.dto.LoginRequest;
import com.copanet.api.seguridad.dto.LoginResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;



@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRecoveryRepository recoveryRepository;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioRecoveryRepository recoveryRepository) {
        this.usuarioRepository = usuarioRepository;
        this.recoveryRepository = recoveryRepository;
    }

    // ====== LOGIN SOLO ADMIN ======
    public LoginResponse login(LoginRequest request) {

        var usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        // 1) validar estado
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new RuntimeException("Usuario inactivo o bloqueado");
        }

        // 2) validar contraseña (comparando texto plano)
        String passwordEnviado = request.getPassword() != null ? request.getPassword() : "";
        String passwordGuardado = usuario.getPasswordHash() != null
                ? new String(usuario.getPasswordHash(), StandardCharsets.UTF_8)
                : "";

        if (!passwordGuardado.equals(passwordEnviado)) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        // 3) validar que sea ADMIN
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));

        if (!esAdmin) {
            throw new RuntimeException("No tiene permisos de administrador");
        }

        var roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());

        return new LoginResponse(usuario.getId(), usuario.getEmail(), usuario.getNombre(), roles);
    }


    // ====== INICIAR RECUPERACIÓN (PASO 1) ======
    public void iniciarRecovery(String email) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

// código de 4 dígitos
String codigo = String.format("%04d", new Random().nextInt(10_000));

        UsuarioRecovery rec = new UsuarioRecovery();
        rec.setId(UUID.randomUUID());               // GUID para UNIQUEIDENTIFIER
        rec.setUsuarioId(usuario.getId());
        rec.setCodigo(codigo);
        rec.setUsado(false);
        rec.setCreadoEn(OffsetDateTime.now());
        rec.setExpiraEn(OffsetDateTime.now().plusMinutes(10));

        recoveryRepository.save(rec);

        System.out.println("Código de recuperación para " + email + ": " + codigo);
    }

    // ====== VALIDAR CÓDIGO (PASO 2) ======
    public void validarCodigo(String email, String codigo) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

        var rec = recoveryRepository
                .findTopByUsuarioIdAndCodigoAndUsadoIsFalseOrderByCreadoEnDesc(
                        usuario.getId(), codigo)
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (OffsetDateTime.now().isAfter(rec.getExpiraEn())) {
            throw new RuntimeException("El código ha expirado");
        }

        // Solo validamos, no cambiamos contraseña aquí todavía
    }

        // ====== RESET DE CONTRASEÑA (PASO 3) ======
    public void resetPassword(String email, String codigo, String nuevaPassword) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var rec = recoveryRepository
                .findTopByUsuarioIdAndCodigoAndUsadoIsFalseOrderByCreadoEnDesc(
                        usuario.getId(), codigo)
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (OffsetDateTime.now().isAfter(rec.getExpiraEn())) {
            throw new RuntimeException("El código ha expirado");
        }

        // marcar código como usado
        rec.setUsado(true);
        recoveryRepository.save(rec);

        // guardar nueva contraseña (como texto en bytes)
        usuario.setPasswordHash(nuevaPassword.getBytes(StandardCharsets.UTF_8));
        usuarioRepository.save(usuario);
    }

}
