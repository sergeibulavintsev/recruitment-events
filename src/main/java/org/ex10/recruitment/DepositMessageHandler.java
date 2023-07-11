package org.ex10.recruitment;

import org.ex10.recruitment.base.Deposit;
import org.ex10.recruitment.base.ExternalSystem;
import org.ex10.recruitment.base.Message;
import org.ex10.recruitment.base.MessageHandler;

import java.util.Optional;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static org.ex10.recruitment.DepositModel.deposit;
import static org.ex10.recruitment.DepositState.CREATED;
import static org.ex10.recruitment.DepositState.SUBMITTED;

public class DepositMessageHandler extends MessageHandler<Deposit> {

    private final Logger LOG = Logger.getLogger(DepositMessageHandler.class.getCanonicalName());

    private final ExternalSystem externalSystem;
    private final DepositPersistence depositPersistence;

    public DepositMessageHandler(ExternalSystem externalSystem, DepositPersistence depositPersistence) {
        this.externalSystem = externalSystem;
        this.depositPersistence = depositPersistence;
    }

    @Override
    public void handleMessage(Message<Deposit> message) {
        final var event = requireNonNull(message.event(), "event is required");

        final var depositToSubmit = depositForSubmission(event);
        depositToSubmit.ifPresent(it -> submitDeposit(it, event));
    }

    private Optional<DepositModel> depositForSubmission(Deposit event) {
        final var maybeExisting = depositPersistence.find(event.id());
        if (maybeExisting.isPresent()) {
            return maybeExisting.filter(it -> !it.is(SUBMITTED))
                    .map(this::validateExisting);
        }
        return of(createDeposit(event));
    }

    private DepositModel validateExisting(DepositModel depositModel) {
        if (depositModel.is(CREATED)) {
            throw new IllegalStateException("Deposit with id=[%s] was created, but not submitted or failed"
                    .formatted(depositModel.id()));
        }
        return depositModel;
    }

    private DepositModel createDeposit(Deposit event) {
        return depositPersistence.persist(deposit(event.id(), CREATED, event.amount(), event.account()));
    }

    private void submitDeposit(DepositModel depositModel, Deposit deposit) {
        try {
            externalSystem.submitDeposit(deposit);
            depositPersistence.persist(depositModel.submit());
        } catch (Exception exception) {
            LOG.severe("Submit deposit=[%s] has failed, retry later".formatted(deposit.id()));
            depositPersistence.persist(depositModel.fail());
            throw exception;
        }
    }
}
