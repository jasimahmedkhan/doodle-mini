package com.minidoodle.schedular.slot.infrastructure.persistence;

import com.minidoodle.schedular.shared.domain.SlotId;
import com.minidoodle.schedular.shared.domain.TimeRange;
import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.slot.domain.SlotRepository;
import com.minidoodle.schedular.slot.domain.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SlotRepositoryAdapter implements SlotRepository {

    private final SlotPersistenceRepository repository;

    public SlotRepositoryAdapter(SlotPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TimeSlot> findById(SlotId id) {
        return repository.findById(id.value()).map(SlotRepositoryAdapter::toDomain);
    }

    @Override
    public TimeSlot save(TimeSlot slot) {
        return toDomain(repository.saveAndFlush(toEntity(slot)));
    }

    @Override
    public void delete(SlotId id) {
        repository.deleteById(id.value());
        repository.flush();
    }

    @Override
    public boolean existsOverlapping(UserId owner, TimeRange timeRange) {
        return repository.existsOverlapping(owner.value(), timeRange.start(), timeRange.end());
    }

    @Override
    public List<TimeSlot> findByOwnerAndOverlapping(UserId owner, TimeRange timeRange) {
        return repository.findOverlapping(owner.value(), timeRange.start(), timeRange.end()).stream()
                .map(SlotRepositoryAdapter::toDomain)
                .toList();
    }

    private static SlotEntity toEntity(TimeSlot slot) {
        return new SlotEntity(
                slot.id().value(),
                slot.owner().value(),
                slot.timeRange().start(),
                slot.timeRange().end(),
                slot.status(),
                slot.version()
        );
    }

    private static TimeSlot toDomain(SlotEntity entity) {
        return new TimeSlot(
                new SlotId(entity.getId()),
                new UserId(entity.getOwnerId()),
                new TimeRange(entity.getStart(), entity.getEnd()),
                entity.getStatus(),
                entity.getVersion()
        );
    }
}
