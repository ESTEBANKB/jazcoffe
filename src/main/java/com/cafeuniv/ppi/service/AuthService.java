package com.cafeuniv.ppi.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.repository.UsuarioRepository;

/**
 * Autenticación de usuarios con BCrypt.
 */
public class AuthService {
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    /**
     * Autentica por email y contraseña.
     * @return usuario válido o null
     */
    public Usuario authenticate(String emailOrUser, String password) {
        if (emailOrUser == null || password == null) return null;
        Usuario user = usuarioRepository.findByEmail(emailOrUser);
        if (user == null || !user.isActivo()) return null;
        BCrypt.Result res = BCrypt.verifyer().verify(password.toCharArray(), user.getHash());
        return res.verified ? user : null;
    }
}


