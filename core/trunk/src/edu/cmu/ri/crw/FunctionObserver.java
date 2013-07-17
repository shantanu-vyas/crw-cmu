/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw;

/**
 * Represents the outcome of an asynchronous function call.
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface FunctionObserver<V> {
    public enum FunctionError {
        CANCELLED, // TODO: fill in useful codes here
        ERROR,
        TIMEOUT;
    }

    public void completed(V result);
    public void failed(FunctionError cause);
}
