package ex10.org.recruitment;

import org.junit.jupiter.api.Test;

import static org.ex10.recruitment.DepositModel.deposit;
import static org.ex10.recruitment.DepositState.*;
import static org.junit.jupiter.api.Assertions.*;

public class DepositModelTest {

    @Test
    void should_throw_exception_when_empty_id() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> deposit("", CREATED, 12, "test"));

        // then
        assertEquals(exception.getMessage(), "id is required and must be not empty");
    }

    @Test
    void should_throw_exception_when_amount_is_non_positive() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> deposit("123", CREATED, 0, "test"));

        // then
        assertEquals(exception.getMessage(), "Amount must be greater than zero");
    }


    @Test
    void should_throw_exception_when_empty_account() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> deposit("123", CREATED, 12, ""));

        // then
        assertEquals(exception.getMessage(), "account is required and must be not empty");
    }

    @Test
    void should_return_submitted() {
        // given
        var deposit = deposit("123", CREATED, 12, "2345");

        // when
        var result = deposit.submit();

        // then
        assertEquals(deposit("123", SUBMITTED, 12, "2345"), result);
    }

    @Test
    void should_return_failed() {
        // given
        var deposit = deposit("123", CREATED, 12, "2345");

        // when
        var result = deposit.fail();

        // then
        assertEquals(deposit("123", FAILED, 12, "2345"), result);
    }

    @Test
    void should_return_same_object_when_failed() {
        // given
        var deposit = deposit("123", FAILED, 12, "2345");

        // when
        var result = deposit.fail();

        // then
        assertSame(deposit, result);
    }
}
