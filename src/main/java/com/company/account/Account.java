package com.company.account;

import com.company.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double balance;
    private boolean overdraftAllowed;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @ManyToOne
    private User user;
    public Account(AccountDto dto, User user) {
        this.id = dto.getId();
        this.balance = dto.getBalance();
        this.overdraftAllowed = dto.isOverdraftAllowed();
        this.type = AccountType.valueOf(dto.getType());
        this.user = user;
    }

}
