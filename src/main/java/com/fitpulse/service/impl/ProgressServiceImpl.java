package com.fitpulse.service.impl;

import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.model.entity.ProgressRecord;
import com.fitpulse.model.entity.User;
import com.fitpulse.repository.ProgressRecordRepository;
import com.fitpulse.service.ProgressService;
import com.fitpulse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProgressServiceImpl implements ProgressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressServiceImpl.class);

    private final ProgressRecordRepository progressRecordRepository;
    private final UserService userService;

    public ProgressServiceImpl(ProgressRecordRepository progressRecordRepository, UserService userService) {
        this.progressRecordRepository = progressRecordRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressResponse> getCurrentUserProgress() {
        return progressRecordRepository.findAllByUserOrderByRecordedAtDesc(currentUser())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressResponse getCurrentUserProgress(UUID id) {
        return ownedRecord(id);
    }

    @Override
    @Transactional
    public void create(ProgressFormRequest request) {
        User user = currentUser();
        if (progressRecordRepository.existsByUserAndRecordedAt(user, request.getRecordedAt())) {
            throw new ProgressServiceException("Вече има запис за прогрес за тази дата.");
        }
        ProgressRecord record = new ProgressRecord();
        record.setUser(user);
        fill(record, request);
        progressRecordRepository.save(record);
        LOGGER.info("User {} created progress record {}", user.getId(), record.getId());
    }

    @Override
    @Transactional
    public void update(UUID id, ProgressFormRequest request) {
        User user = currentUser();
        ProgressRecord record = ownedRecord(id, user);
        if (progressRecordRepository.existsByUserAndRecordedAtAndIdNot(user, request.getRecordedAt(), id)) {
            throw new ProgressServiceException("Вече има запис за прогрес за тази дата.");
        }
        fill(record, request);
        progressRecordRepository.save(record);
        LOGGER.info("User {} updated progress record {}", user.getId(), id);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = currentUser();
        progressRecordRepository.delete(ownedRecord(id, user));
        LOGGER.info("User {} deleted progress record {}", user.getId(), id);
    }

    private ProgressResponse ownedRecord(UUID id) {
        User user = currentUser();
        return toResponse(ownedRecord(id, user));
    }

    private ProgressRecord ownedRecord(UUID id, User user) {
        return progressRecordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ProgressServiceException("Записът за прогрес не е намерен."));
    }

    private User currentUser() {
        return userService.getCurrentUser();
    }

    private void fill(ProgressRecord record, ProgressFormRequest request) {
        record.setWeight(request.getWeight());
        record.setBodyFatPercentage(request.getBodyFatPercentage());
        record.setRecordedAt(request.getRecordedAt());
        record.setNote(request.getNote() == null ? null : request.getNote().trim());
    }

    private ProgressResponse toResponse(ProgressRecord record) {
        return new ProgressResponse(record.getId(), record.getUser().getId(), record.getWeight(),
                record.getBodyFatPercentage(), record.getRecordedAt(), record.getNote(),
                record.getCreatedAt(), record.getUpdatedAt());
    }
}
