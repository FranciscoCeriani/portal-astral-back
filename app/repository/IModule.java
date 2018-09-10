package repository;

import models.Student;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface IModule<T> {

    CompletionStage<Optional<Boolean>> update(String id, T entity);

    CompletionStage<Optional<Boolean>> delete(String id);

    CompletionStage<String> insert(T entity);

    CompletionStage<Optional<T>> get(String id);

    CompletionStage<Optional<List<T>>> getAll();
}
