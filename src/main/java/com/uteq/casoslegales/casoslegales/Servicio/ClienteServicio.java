package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Cliente;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServicio {

    @Autowired
    private ClienteRepositorio clienteRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Cliente> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return clienteRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (clienteRepo.existsById(id)) {
            clienteRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean existePorUsuarioId(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return clienteRepo.existsByUsuario(usuario);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Cliente> listarTodos() {
        return clienteRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Cliente guardar(Cliente cliente) {
        return clienteRepo.save(cliente);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        clienteRepo.deleteById(id);
    }
}
