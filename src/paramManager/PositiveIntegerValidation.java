package paramManager;

import com.beust.jcommander.*;

/**
 * La classe PositiveIntegerValidation implementa la interfície
 *  * {@code IParameterValidator} de JCommander. La seva finalitat és
 *  comprovar si el valor el paràmetre rebut és un valor enter positiu.
 *
 *  @see <a href="https://github.com/cbeust/jcommander/blob/master/src/main/java/com/beust/jcommander/IParameterValidator.java">IParameterValidator</a>
 */
public class PositiveIntegerValidation implements IParameterValidator {

    /**
     * Comprova si el paràmetre {@code nom} rebut, {@code value}
     * és une valor enter positiu.
     * @param nom
     *        Nom del paràmetre que es vol validar.
     * @param value
     *        String rebut pel paràmetre inicial.
     * @throws ParameterException
     *         Si el {@code value} no és un valor enter positiu.
     */
    @Override
    public void validate(String nom, String value) throws ParameterException {
        try {
            int n = Integer.parseInt(value);
            if (n < 0) {
                throw new ParameterException("param " + nom + ":\n\t" + "Ha de ser un valor enter positiu.");
            }
        } catch (NumberFormatException e) {
            throw new ParameterException("param " + nom + ":\n\t" + "Ha de ser un valor enter positiu.");
        }
    }
}
