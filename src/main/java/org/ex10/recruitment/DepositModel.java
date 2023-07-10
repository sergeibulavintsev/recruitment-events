package org.ex10.recruitment;

import static org.ex10.recruitment.DepositState.*;

public record DepositModel(String id, DepositState state, long amount, String account) {

    public static DepositModel createdDeposit(String id, long amount, String account) {
        return new DepositModel(id, CREATED, amount, account);
    }

    public boolean is(DepositState state) {
        return this.state == state;
    }

    public DepositModel submit() {
        return new DepositModel(id, SUBMITTED, amount, account);
    }

    public DepositModel fail() {
        if (is(FAILED)) {
            return this;
        }
        return new DepositModel(id, FAILED, amount, account);
    }
}
