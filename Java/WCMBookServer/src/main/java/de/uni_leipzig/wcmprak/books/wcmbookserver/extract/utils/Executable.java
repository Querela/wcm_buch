package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils;

/**
 * Interface for marking a module as executable (that the module has a single method for execution).
 * Created by Erik on 28.11.2014.
 */
public interface Executable {
    /**
     * Main method for running the process/module logic.
     */
    public void run();
}
