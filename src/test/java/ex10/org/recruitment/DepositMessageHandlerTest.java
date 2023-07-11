package ex10.org.recruitment;

import org.ex10.recruitment.DepositMessageHandler;
import org.ex10.recruitment.DepositModel;
import org.ex10.recruitment.DepositPersistence;
import org.ex10.recruitment.DepositState;
import org.ex10.recruitment.base.Deposit;
import org.ex10.recruitment.base.ExternalSystem;
import org.ex10.recruitment.base.Message;
import org.junit.jupiter.api.Test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.ex10.recruitment.DepositModel.deposit;
import static org.ex10.recruitment.DepositState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

class DepositMessageHandlerTest {

    ExternalSystem externalSystem = mock(ExternalSystem.class);
    DepositPersistence depositPersistence = mock(DepositPersistence.class);
    DepositMessageHandler handler = new DepositMessageHandler(externalSystem, depositPersistence);

    @Test
    void should_successfully_submit_deposit() {
        // given
        var deposit = new Deposit(randomUUID().toString(), "account", 1000);
        var createdDeposit = depositInState(deposit, CREATED);
        var submittedDeposit = depositInState(deposit, SUBMITTED);
        var event = new Message<>(deposit, 10);

        given(depositPersistence.persist(createdDeposit)).willReturn(createdDeposit);
        given(depositPersistence.find(deposit.id())).willReturn(empty());

        // when
        handler.handleMessage(event);

        // then
        verify(externalSystem).submitDeposit(deposit);
        verify(depositPersistence).persist(createdDeposit);
        verify(depositPersistence).persist(submittedDeposit);
    }

    @Test
    void should_do_nothing_when_deposit_is_already_submitted() {
        // given
        var deposit = new Deposit(randomUUID().toString(), "account", 1000);
        var submittedDeposit = depositInState(deposit, SUBMITTED);
        var event = new Message<>(deposit, 10);

        given(depositPersistence.find(deposit.id())).willReturn(of(submittedDeposit));

        // when
        handler.handleMessage(event);

        // then
        verifyNoInteractions(externalSystem);
        verify(depositPersistence).find(deposit.id());
        verifyNoMoreInteractions(depositPersistence);
    }

    @Test
    void should_throw_exception_when_existing_deposit_in_created_state() {
        // given
        var deposit = new Deposit(randomUUID().toString(), "account", 1000);
        var existingCreatedDeposit = depositInState(deposit, CREATED);
        var event = new Message<>(deposit, 10);

        given(depositPersistence.find(deposit.id())).willReturn(of(existingCreatedDeposit));

        // when
        var exception = assertThrows(IllegalStateException.class, () -> handler.handleMessage(event));

        // then
        assertEquals("Deposit with id=[%s] was created, but not submitted or failed"
                .formatted(existingCreatedDeposit.id()), exception.getMessage());
        verifyNoInteractions(externalSystem);
        verify(depositPersistence).find(deposit.id());
        verifyNoMoreInteractions(depositPersistence);
    }

    @Test
    void should_successfully_submit_previously_failed_deposit() {
        // given
        var deposit = new Deposit(randomUUID().toString(), "account", 1000);
        var existingFailedDeposit = depositInState(deposit, FAILED);
        var submittedDeposit = depositInState(deposit, SUBMITTED);
        var event = new Message<>(deposit, 10);

        given(depositPersistence.find(deposit.id())).willReturn(of(existingFailedDeposit));

        // when
        handler.handleMessage(event);

        // then
        verify(externalSystem).submitDeposit(deposit);
        verify(depositPersistence).find(deposit.id());
        verify(depositPersistence).persist(submittedDeposit);
    }

    @Test
    void should_fail_deposit_when_exception_thrown_by_external_system() {
        // given
        var deposit = new Deposit(randomUUID().toString(), "account", 1000);
        var createdDeposit = depositInState(deposit, CREATED);
        var failedDeposit = depositInState(deposit, FAILED);
        var event = new Message<>(deposit, 10);
        var externalSystemException = new RuntimeException("External system error");

        given(depositPersistence.find(deposit.id())).willReturn(empty());
        given(depositPersistence.persist(createdDeposit)).willReturn(createdDeposit);
        willThrow(externalSystemException).given(externalSystem).submitDeposit(deposit);

        // when
        var exception = assertThrows(RuntimeException.class, () -> handler.handleMessage(event));

        // then
        assertEquals(externalSystemException, exception);
        verify(depositPersistence).persist(createdDeposit);
        verify(depositPersistence).persist(failedDeposit);
    }

    private static DepositModel depositInState(Deposit deposit, DepositState state) {
        return deposit(deposit.id(), state, deposit.amount(), deposit.account());
    }
}