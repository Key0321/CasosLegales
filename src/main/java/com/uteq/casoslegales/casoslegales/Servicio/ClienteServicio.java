package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Cliente;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServicio {

    @Autowired
    private ClienteRepositorio clienteRepo;

    public Page<Cliente> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return clienteRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (clienteRepo.existsById(id)) {
            clienteRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existePorUsuarioId(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return clienteRepo.existsByUsuario(usuario);
    }

    public List<Cliente> listarTodos() {
        return clienteRepo.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepo.findById(id);
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepo.save(cliente);
    }

    public void eliminar(Long id) {
        clienteRepo.deleteById(id);
    }
}
