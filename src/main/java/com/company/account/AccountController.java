package com.company.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/user/{userId}")
    ResponseEntity<List<Account>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(accountService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(accountService.getOne(id));
    }

    @PostMapping
    ResponseEntity<AccountDto> create(@RequestBody AccountDto dto) {
        return ResponseEntity.ok(accountService.create(dto));

    }

    @PatchMapping("/{id}")
    ResponseEntity<AccountDto> update(@PathVariable Integer id, @RequestBody Account updatedAccount) {
        return ResponseEntity.ok(accountService.update(id, updatedAccount));
    }

    @PatchMapping("/deposit/{id}")
    ResponseEntity<AccountDto> depositMoney(@PathVariable Integer id, @RequestBody AmountDto dto) {
        return ResponseEntity.ok(accountService.depositMoney(id, dto));
    }

    @PatchMapping("/withdraw/{id}")
    ResponseEntity<AccountDto> withdrawMoney(@PathVariable Integer id, @RequestBody AmountDto dto) {
        return ResponseEntity.ok(accountService.withdrawMoney(id, dto.getAmount()));
    }

    @DeleteMapping("/{id}")
    void deleteAccount(@PathVariable Integer id) {
        accountService.delete(id);
    }
}
