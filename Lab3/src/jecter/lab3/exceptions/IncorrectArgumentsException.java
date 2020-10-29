package jecter.lab3.exceptions;

public class IncorrectArgumentsException extends Exception {
    @Override
    public String toString() {
        return EXC_STR;
    }

    private static final String EXC_STR = "Incorrect arguments";
}
