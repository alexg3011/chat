package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Room;
import ru.job4j.service.RoomService;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/")
    public List<Room> findAll() {
        return StreamSupport.stream(
                this.roomService.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        var room = this.roomService.findById(id);
        HttpStatus httpStatus = room.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<Room>(
                room.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Комната не найдена")), httpStatus);
    }

    @PostMapping("/")
    public ResponseEntity<Room> create(@Valid @RequestBody Room room) {
        if (room.getName() == null) {
            throw new NullPointerException("Не введено название комнаты");
        }
        return new ResponseEntity<>(
                this.roomService.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@Valid @RequestBody Room room) {
        if (room.getName() == null) {
            throw new NullPointerException("Не введено название комнаты");
        }
        this.roomService.save(room);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        this.roomService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/roomPatch")
    public ResponseEntity<Room> patchUser(@Valid @RequestBody Room room) throws InvocationTargetException, IllegalAccessException {
        var current = roomService.findById(room.getId())
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
                var newValue = getMethod.invoke(room);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        return new ResponseEntity<>(
                roomService.save(current), HttpStatus.OK
        );
    }
}