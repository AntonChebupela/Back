package com.company.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    private Integer id;
    private double balance;
    private boolean overdraftAllowed;
    private String type;
    private Integer userId;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.balance = account.getBalance();
        this.overdraftAllowed = account.isOverdraftAllowed();
        this.type = account.getType().toString();
        this.userId = account.getUser().getId();
    }
}
