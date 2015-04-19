package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils;

/**
 * Marks a class so that initialize should be called prior to execution.
 * Created by Erik on 28.11.2014.
 */
public interface Initializable {
    /**
     * Can be used to check if a module has been initialized.
     *
     * @return true if {@link #initialize()} has been called and the module has been initialized.
     */
    public boolean hasBeenInitialized();

    /**
     * Initialize the module.
     *
     * @throws Exception
     */
    public void initialize() throws Exception;
}
