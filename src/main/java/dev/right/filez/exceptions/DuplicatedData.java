package dev.right.filez.exceptions;

// lk, I think this is pointless.
public class DuplicatedData extends RuntimeException {
    public DuplicatedData(String message) {
        super(message);
    }
}
