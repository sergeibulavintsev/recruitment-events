package org.ex10.recruitment;

import java.util.Optional;

public interface DepositPersistence {

    void beginTransaction();

    void commitTransaction();

    void rollbackTransaction();

    Optional<DepositModel> find(String id);

    DepositModel persist(DepositModel model);
}
