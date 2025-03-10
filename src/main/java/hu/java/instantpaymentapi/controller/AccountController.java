package hu.java.instantpaymentapi.controller;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Account> getAccountBalance(@PathVariable("accountId") String accountId) {

        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}
