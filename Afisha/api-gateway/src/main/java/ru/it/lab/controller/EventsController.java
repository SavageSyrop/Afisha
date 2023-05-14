package ru.it.lab.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.dto.EventDTO;
import ru.it.lab.enums.EventType;

import java.util.Date;

@RestController
@RequestMapping("/events")
public class EventsController {

    @GrpcClient("grpc-event-service")
    private  EventServiceGrpc.EventServiceBlockingStub eventService;

    @GetMapping("/{eventId}")
    public String getEvent(@PathVariable Long eventId) {

    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String addEvent(@RequestBody EventDTO eventDTO) {

    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String deleteEvent(@PathVariable Long eventId) {

    }

    @GetMapping("/select")
    public String selectByType(@RequestParam EventType eventType) {

    }

    @GetMapping("/select/period")
    public String selectByType(@RequestParam Date start, @RequestParam Date end) {

    }

    @GetMapping("/{eventId}/comments")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getComments(@PathVariable Long eventId, @RequestBody String comment) {

    }

    @PostMapping("/{eventId}/comments")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String commentEvent(@PathVariable Long eventId, @RequestBody String comment) {

    }

    @DeleteMapping("/{eventId}/comment/{commentId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String deleteComment(@PathVariable Long eventId, @RequestBody String comment) {

    }

    @PostMapping("/{eventId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String addFavorites(@PathVariable Long eventId) {

    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String deleteFavorites(@PathVariable Long eventId) {

    }


    @GetMapping("/{eventId}/rate")
    public String getRating(@PathVariable Long eventId) {

    }

    @PostMapping("/{eventId}/rate") // TODO toggle add.dekete
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String rateEvent(@PathVariable Long eventId) {

    }

}
