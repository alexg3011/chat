package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Message;
import ru.job4j.service.MessageService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return StreamSupport.stream(
                this.messageService.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable int id) {
        var message = this.messageService.findById(id);
        HttpStatus httpStatus = message.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(
                message.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сообщение не найдено!")),
                httpStatus);
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Нельзя ввести пустое сообщение!");
        }
        return new ResponseEntity<>(
                this.messageService.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Нельзя ввести пустое сообщение!");
        }

        this.messageService.save(message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        this.messageService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
