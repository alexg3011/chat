package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Role;
import ru.job4j.service.RoleService;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/")
    public List<Role> findAll() {
        return StreamSupport.stream(
                this.roleService.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(@PathVariable int id) {
        var role = roleService.findById(id);
        HttpStatus httpStatus = role.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<Role>(
                role.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Роль не найдена!")), httpStatus);
    }

    @PostMapping("/")
    public ResponseEntity<Role> create(@Valid @RequestBody Role role) {
        if (role.getName() == null) {
            throw new NullPointerException("Введите название роли!");
        }
        return new ResponseEntity<>(
                roleService.save(role),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@Valid @RequestBody Role role) {
        if (role.getName() == null) {
            throw new NullPointerException("Введите название роли!");
        }
        this.roleService.save(role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        roleService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rolePatch")
    public ResponseEntity<Role> patchUser(@Valid  @RequestBody Role role) throws InvocationTargetException, IllegalAccessException {
        var current = roleService.findById(role.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var methods = current.getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method : methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }
        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid properties mapping");
                }
                var newValue = getMethod.invoke(role);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        return new ResponseEntity<>(
                roleService.save(current), HttpStatus.OK
        );
    }
}
