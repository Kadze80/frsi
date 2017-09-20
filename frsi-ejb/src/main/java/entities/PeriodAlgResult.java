package entities;

import java.io.Serializable;

/**
 * Представляет результат выполнения алгоритмов предоставления отчетов.
 * Если значение поля hasError равен true, значит в алгоритме ошибка. А само сообщение об ошибке пишется в поле errorMessage
 * Если значение поля hasError равен false, значит алгоритм выполнился без ошибок. Результат пишется в поле result. В поле errorMessage присваивается пустое значение.
 * Если результат имеет положительное число значит к сроку осталось дней данное количество дней.
 * если результат отрицательное число, тогда срок сдачи просрочен на данное количество дней.
 */
public class PeriodAlgResult implements Serializable {

    public static PeriodAlgResult error(String errorMessage) {
        return new PeriodAlgResult(0, true, errorMessage);
    }

    public static PeriodAlgResult success(Integer result) {
        return new PeriodAlgResult(result, false, "");
    }

    private int result;
    private boolean hasError;
    private String errorMessage;

    private PeriodAlgResult(int result, boolean hasError, String errorMessage) {
        this.result = result;
        this.hasError = hasError;
        this.errorMessage = errorMessage;
    }

    public int getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return hasError;
    }
}
