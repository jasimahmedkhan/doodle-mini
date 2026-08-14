package com.minidoodle.schedular.meeting.infrastructure.persistence;

import com.minidoodle.schedular.meeting.domain.Meeting;
import com.minidoodle.schedular.meeting.domain.MeetingRepository;
import com.minidoodle.schedular.meeting.domain.Participant;
import com.minidoodle.schedular.shared.domain.MeetingId;
import com.minidoodle.schedular.shared.domain.SlotId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MeetingRepositoryAdapter implements MeetingRepository {

    private final MeetingPersistenceRepository repository;

    public MeetingRepositoryAdapter(MeetingPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Meeting> findById(MeetingId id) {
        return repository.findById(id.value()).map(MeetingRepositoryAdapter::toDomain);
    }

    @Override
    public Meeting save(Meeting meeting) {
        return toDomain(repository.saveAndFlush(toEntity(meeting)));
    }

    @Override
    public void delete(MeetingId id) {
        repository.deleteById(id.value());
        repository.flush();
    }

    @Override
    public Optional<Meeting> findBySlotId(SlotId slotId) {
        return repository.findBySlotId(slotId.value()).map(MeetingRepositoryAdapter::toDomain);
    }

    private static MeetingEntity toEntity(Meeting meeting) {
        return new MeetingEntity(
                meeting.id().value(),
                meeting.slotId().value(),
                meeting.title(),
                meeting.description(),
                meeting.participants().stream()
                        .map(participant -> new MeetingParticipantEmbeddable(
                                participant.name(),
                                participant.email()
                        ))
                        .toList()
        );
    }

    private static Meeting toDomain(MeetingEntity entity) {
        return new Meeting(
                new MeetingId(entity.getId()),
                new SlotId(entity.getSlotId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getParticipants().stream()
                        .map(participant -> new Participant(participant.getName(), participant.getEmail()))
                        .toList()
        );
    }
}
