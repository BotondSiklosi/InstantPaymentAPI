package hu.java.instantpaymentapi.service;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getAccount(String accountId) {

        return accountRepository.findByAccountId(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
    }

}
