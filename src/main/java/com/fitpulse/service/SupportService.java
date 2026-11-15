package com.fitpulse.service;

import com.fitpulse.model.dto.SupportForm;
import com.fitpulse.model.entity.SupportMessage;
import com.fitpulse.model.enums.*;
import com.fitpulse.repository.SupportMessageRepository;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

@Service
public class SupportService {
    private static final Logger LOG=LoggerFactory.getLogger(SupportService.class);
    private final SupportMessageRepository messages;
    private final SupportImageStorage storage;
    public SupportService(SupportMessageRepository messages,SupportImageStorage storage){this.messages=messages;this.storage=storage;}
    @Transactional(rollbackFor=IOException.class)
    public void submit(SupportForm form,MultipartFile photo) throws IOException {
        String key=storage.store(photo);
        if(key!=null) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCompletion(int status) {
                if(status!=STATUS_COMMITTED) try {storage.delete(key);} catch(IOException e){LOG.error("Cannot clean up rolled-back support attachment {}",key,e);}
            }
        });
        messages.save(new SupportMessage(form.getCategory(),form.getSubject(),form.getDescription(),form.getReplyEmail(),key));
    }
    @Transactional(readOnly=true)
    public Page<SupportMessage> list(SupportCategory category,SupportStatus status,int page) {
        var requested=PageRequest.of(Math.max(0,page),25,Sort.by(Sort.Direction.DESC,"createdAt","id"));
        var result=fetch(category,status,requested);
        // A stale/out-of-range page must not show an empty inbox when matching messages exist.
        if(result.isEmpty() && result.getTotalElements()>0 && requested.getPageNumber()>0)
            return fetch(category,status,requested.withPage(0));
        return result;
    }
    private Page<SupportMessage> fetch(SupportCategory category,SupportStatus status,Pageable page) {
        // No selected filters means ALL statuses, including NEW. Do not bind null enum predicates.
        if(category==null && status==null)return messages.findAll(page);
        if(category==null)return messages.findByStatus(status,page);
        if(status==null)return messages.findByCategory(category,page);
        return messages.findByCategoryAndStatus(category,status,page);
    }
    @Transactional(readOnly=true)
    public SupportMessage get(Long id){return messages.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
    @Transactional
    public void status(Long id,SupportStatus status){get(id).setStatus(status);}
}
