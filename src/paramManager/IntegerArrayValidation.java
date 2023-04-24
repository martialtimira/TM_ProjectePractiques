package paramManager;

import com.beust.jcommander.*;

import java.util.regex.PatternSyntaxException;

/**
 * La classe IntegerArrayValidation implementa la interfície
 *  * {@code IParameterValidator} de JCommander. La seva finalitat és
 *  comprovar si el valor el paràmetre rebut és un conjunt d'enters positius.
 *
 *  @see <a href="https://github.com/cbeust/jcommander/blob/master/src/main/java/com/beust/jcommander/IParameterValidator.java">IParameterValidator</a>
 */
public class IntegerArrayValidation implements IParameterValidator {

    /**
     * Comprova si el paràmetre {@code nom} rebut, {@code value}
     * és une conjunt d'enters positius
     * @param nom
     *        Nom del paràmetre que es vol validar.
     * @param value
     *        String rebut pel paràmetre inicial.
     * @throws ParameterException
     *         Si el {@code value} no separa els valors en espais, o
     *         si el {@code value} no és un conjunt d'enters positius.
     */
    @Override
    public void validate(String nom, String value) throws ParameterException {
        try {
            String[] values = value.split(" ");
            int n;

            for(String s : values) {
                n = Integer.parseInt(s);
                if (n < 0) throw new NumberFormatException();
            }
        } catch (PatternSyntaxException e) {
            throw new ParameterException("param " + nom + ":\n\t" + "S'ha d'introduir un conjunt de valors, separats per " +
                    "espais. ex: " + nom + "val1 val2 val3 ... valN.");
        } catch (NumberFormatException e) {
            throw new ParameterException("param " + nom + ":\n\t" + "Els valors introduïts han de ser enters positius.");
        }
    }
}
