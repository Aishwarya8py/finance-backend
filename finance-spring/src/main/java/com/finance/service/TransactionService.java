package com.finance.service;

import com.finance.dto.TransactionDto;
import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.enums.TransactionType;
import com.finance.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.finance.repository.TransactionRepository;
import com.finance.repository.TransactionSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public Page<Transaction> list(
            TransactionType type, String category,
            LocalDate dateFrom, LocalDate dateTo,
            String search, int page, int limit) {

        var spec     = TransactionSpec.filter(type, category, dateFrom, dateTo, search);
        var pageable = PageRequest.of(page - 1, limit, Sort.by("date").descending().and(Sort.by("id").descending()));
        return transactionRepository.findAll(spec, pageable);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    @Transactional
    public Transaction create(TransactionDto.CreateRequest req, Long userId) {
        User user = userService.findById(userId);
        Transaction tx = Transaction.builder()
                .user(user)
                .amount(req.getAmount())
                .type(req.getType())
                .category(req.getCategory())
                .date(req.getDate())
                .notes(req.getNotes())
                .build();
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction update(Long id, TransactionDto.UpdateRequest req) {
        Transaction tx = findById(id);
        if (req.getAmount()   != null) tx.setAmount(req.getAmount());
        if (req.getType()     != null) tx.setType(req.getType());
        if (req.getCategory() != null) tx.setCategory(req.getCategory());
        if (req.getDate()     != null) tx.setDate(req.getDate());
        if (req.getNotes()    != null) tx.setNotes(req.getNotes());
        return transactionRepository.save(tx);
    }

    @Transactional
    public void softDelete(Long id) {
        Transaction tx = findById(id);
        tx.setDeleted(true);
        transactionRepository.save(tx);
    }
}
