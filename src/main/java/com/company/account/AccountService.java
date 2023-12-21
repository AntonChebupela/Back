package com.company.account;

import com.company.user.User;
import com.company.user.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.TransactionException;
import org.hibernate.action.internal.EntityActionVetoException;
import org.hibernate.exception.DataException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserService userService;
    private final AccountRepository repository;

    private boolean isTypeValid(String type) {
        return type.equals("CREDIT") ||
                type.equals("DEBIT") ||
                type.equals("SAVING") ||
                type.equals("BROKERAGE");
    }

    private AccountType getType(String stringType) {
        if (isTypeValid(stringType)) {
            return AccountType.valueOf(stringType);
        }
        throw new EntityNotFoundException("Type " + stringType + "not found");
    }

    public List<Account> getByUserId(Integer userId) {
        User user = userService.getById(userId);
        return repository.findAllByUser(user);
    }

    public AccountDto create(AccountDto dto) {
        AccountType type = getType(dto.getType());
        User user = userService.getById(dto.getUserId());
        if (accountNotExist(type, user) && userService.isClient(user)) {
            return getDto(repository.save(new Account(dto, user)));
        } else throw new EntityExistsException("Account already exists");
    }

    private boolean accountNotExist(AccountType type, User user) {
        List<Account> accounts = getByUserId(user.getId());
        for (Account account : accounts) {
            if (account.getType() == type) {
                return false;
            }
        }
        return true;
    }
    public AccountDto getOne(Integer id) {
        return getDto(getById(id));
    }

    public Account getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(getErrorMessage(id)));
    }

    private AccountDto getDto(Account account) {
        return new AccountDto(account);
    }

    private static String getErrorMessage(Integer id) {
        return "Account with id " + id + " not found";
    }

    public AccountDto update(Integer id, Account updatedAccount) {
        return getDto(repository.findById(id).map(account -> {
            account.setBalance(updatedAccount.getBalance());
            account.setType(updatedAccount.getType());
            account.setOverdraftAllowed(updatedAccount.isOverdraftAllowed());
            return repository.save(account);
        }).orElseThrow(() -> new RuntimeException(getErrorMessage(id))));
    }

    public AccountDto depositMoney(Integer id, AmountDto dto) {
        return getDto(repository.findById(id).map(account -> {
            account.setBalance(account.getBalance() + dto.getAmount());
            return repository.save(account);
        }).orElseThrow(() -> new RuntimeException("Failed to deposit money to account")));
    }

    public AccountDto withdrawMoney(Integer id, double amount) {
        Account account = getById(id);
        System.out.println(account);
        double balance = account.getBalance();
        if (balance > amount || account.isOverdraftAllowed()) {
            account.setBalance(account.getBalance() - amount);
        } else {
            throw new TransactionException("You cannot withdraw this amount of money");
        }
        return getDto(repository.save(account));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
