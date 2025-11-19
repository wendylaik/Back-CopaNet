package com.copanet.api.service;

import com.copanet.api.dtos.CrearUsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.CallableStatement;

@Service
public class UsuarioService {

    @Autowired
    private DataSource dataSource;

    public void crearUsuario(CrearUsuarioDTO dto, Integer auditorId) throws Exception {

        int rolId;

        switch (dto.getRol().trim().toLowerCase()) {
            case "administrador":
            case "admin":
                rolId = 1;
                break;

            case "dte":
                rolId = 2;
                break;

            default:
                throw new RuntimeException("Rol inválido");
        }

        try (Connection con = dataSource.getConnection();
             CallableStatement stmt = con.prepareCall("{CALL sp_CrearUsuario(?, ?, ?, ?, ?, ?)}")) {

            stmt.setString(1, dto.getEmail());
            stmt.setString(2, dto.getIdentificacion());
            stmt.setInt(3, rolId);  // ← CORRECTO
            stmt.setBytes(4, dto.getPassword().getBytes());  
            stmt.setString(5, dto.getNombre());
            stmt.setInt(6, auditorId);

            stmt.execute();
        }
    }

    public List<UsuarioListadoDTO> listarUsuarios() {

        var usuarios = usuarioRepository.findAll();

        return usuarios.stream().map(u -> new UsuarioListadoDTO(
                u.getIdentificacion(),
                u.getNombre(),
                u.getRoles().stream().findFirst().map(Rol::getNombre).orElse("Sin rol"),
                u.getEstado()
        )).toList();
    }

}
