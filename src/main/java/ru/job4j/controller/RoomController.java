package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Room;
import ru.job4j.service.RoomService;

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
    public ResponseEntity<Room> create(@RequestBody Room room) {
        if (room.getName() == null) {
            throw new NullPointerException("Не введено название комнаты");
        }
        return new ResponseEntity<>(
                this.roomService.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room) {
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
}