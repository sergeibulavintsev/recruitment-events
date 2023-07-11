package org.ex10.recruitment;

import static java.util.Objects.requireNonNull;
import static org.ex10.recruitment.DepositState.*;
import static org.ex10.recruitment.checks.Checks.checkNotEmpty;
import static org.ex10.recruitment.checks.Checks.checkThat;

public record DepositModel(String id, DepositState state, long amount, String account) {

    public DepositModel {
        checkNotEmpty(id, "id is required and must be not empty");
        checkThat(amount > 0, "Amount must be greater than zero");
        checkNotEmpty(account, "account is required and must be not empty");
    }

    public static DepositModel deposit(String id, DepositState state, long amount, String account) {
        return new DepositModel(id, state, amount, account);
    }

    public boolean is(DepositState state) {
        return this.state == state;
    }

    public DepositModel submit() {
        return deposit(id, SUBMITTED, amount, account);
    }

    public DepositModel fail() {
        if (is(FAILED)) {
            return this;
        }
        return deposit(id, FAILED, amount, account);
    }
}
