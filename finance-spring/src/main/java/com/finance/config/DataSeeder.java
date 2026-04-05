package com.finance.config;

import com.finance.entity.Transaction;
import com.finance.entity.User;
import com.finance.enums.Role;
import com.finance.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;    // already seeded

        // ── Demo users ────────────────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .name("Alice Admin").email("admin@finance.dev")
                .passwordHash(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());

        User analyst = userRepository.save(User.builder()
                .name("Bob Analyst").email("analyst@finance.dev")
                .passwordHash(passwordEncoder.encode("analyst123")).role(Role.ANALYST).build());

        userRepository.save(User.builder()
                .name("Carol Viewer").email("viewer@finance.dev")
                .passwordHash(passwordEncoder.encode("viewer123")).role(Role.VIEWER).build());

        log.info("✔  Seeded 3 demo users");

        // ── Sample transactions ───────────────────────────────────────────────
        List<Transaction> txs = List.of(
            tx(admin,   "5000", TransactionType.INCOME,  "Salary",        "2024-01-31", "January salary"),
            tx(admin,   "1200", TransactionType.EXPENSE, "Rent",          "2024-01-05", "Monthly rent"),
            tx(admin,    "350", TransactionType.EXPENSE, "Groceries",     "2024-01-10", null),
            tx(admin,     "80", TransactionType.EXPENSE, "Utilities",     "2024-01-15", "Electricity bill"),
            tx(admin,   "2500", TransactionType.INCOME,  "Freelance",     "2024-02-12", "Web project"),
            tx(analyst,  "600", TransactionType.EXPENSE, "Subscriptions", "2024-02-20", "Annual SaaS"),
            tx(analyst,  "200", TransactionType.EXPENSE, "Dining",        "2024-03-03", null),
            tx(admin,   "4800", TransactionType.INCOME,  "Salary",        "2024-03-31", "March salary"),
            tx(admin,    "900", TransactionType.EXPENSE, "Travel",        "2024-04-08", "Conference trip"),
            tx(admin,    "150", TransactionType.EXPENSE, "Groceries",     "2024-04-20", null)
        );
        transactionRepository.saveAll(txs);
        log.info("✔  Seeded {} sample transactions", txs.size());
        log.info("─────────────────────────────────────────────");
        log.info("  admin@finance.dev    / admin123");
        log.info("  analyst@finance.dev  / analyst123");
        log.info("  viewer@finance.dev   / viewer123");
        log.info("─────────────────────────────────────────────");
    }

    private Transaction tx(User user, String amount, TransactionType type,
                            String category, String date, String notes) {
        return Transaction.builder()
                .user(user)
                .amount(new BigDecimal(amount))
                .type(type)
                .category(category)
                .date(LocalDate.parse(date))
                .notes(notes)
                .build();
    }
}
