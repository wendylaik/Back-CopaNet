package com.copanet.api.controller;

import com.copanet.api.dtos.CrearUsuarioDTO;
import com.copanet.api.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(
            @RequestBody CrearUsuarioDTO dto,
            @RequestAttribute("usuarioId") Integer auditorId) {

        try {
            usuarioService.crearUsuario(dto, auditorId);
            return ResponseEntity.ok("Usuario creado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/listar")
    public List<UsuarioListadoDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

}
