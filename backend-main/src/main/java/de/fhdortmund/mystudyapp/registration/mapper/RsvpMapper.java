package de.fhdortmund.mystudyapp.registration.mapper;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.identity.mapper.UserMapper;
import de.fhdortmund.mystudyapp.registration.dto.RsvpDto;
import de.fhdortmund.mystudyapp.registration.model.Rsvp;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RsvpMapper {

    private final UserMapper userMapper;

    public RsvpDto toDto(Rsvp rsvp) {
        if (rsvp == null) return null;
        return RsvpDto.builder()
                .id(rsvp.getId())
                .eventId(rsvp.getEvent().getId())
                .eventTitle(rsvp.getEvent().getTitle())
                .user(userMapper.toDto(rsvp.getUser()))
                .status(rsvp.getStatus())
                .createdAt(rsvp.getCreatedAt())
                .build();
    }
}