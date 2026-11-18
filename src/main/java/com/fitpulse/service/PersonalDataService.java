package com.fitpulse.service;

import com.fitpulse.model.entity.*;
import com.fitpulse.repository.ProgressRecordRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Weight has one source of truth: the existing progress history. */
@Service
public class PersonalDataService {
    private final ProgressRecordRepository records;
    @PersistenceContext private EntityManager entities;
    public PersonalDataService(ProgressRecordRepository records){this.records=records;}
    @Transactional(readOnly=true)
    public BigDecimal weight(User user){return records.findFirstByUserOrderByRecordedAtDesc(user).map(ProgressRecord::getWeight).orElse(null);}
    @Transactional
    public void saveWeight(User user, BigDecimal weight){
        if(weight==null)return;
        // Serialize profile/nutrition updates before upserting today's unique record.
        User managed=entities.find(User.class,user.getId(),LockModeType.PESSIMISTIC_WRITE);
        BigDecimal previous=weight(managed);
        if(previous!=null&&previous.compareTo(weight)==0)return;
        ProgressRecord record=records.findByUserAndRecordedAt(managed,LocalDate.now()).orElseGet(ProgressRecord::new);
        record.setUser(managed);record.setRecordedAt(LocalDate.now());record.setWeight(weight);records.save(record);
    }
}
