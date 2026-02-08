package com.utgaming.testconcurrency.service;

public interface IdCheckStrategy {
    boolean exists(Long id);

    default void add(Long id){}
}
