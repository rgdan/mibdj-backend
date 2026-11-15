package teccr.mibdj.repository;

import teccr.mibdj.data.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findAll();
    Optional<Task> findById(Long id);
    List<Task> findByUserId(Long userId);
    Task save(Task task);
    void deleteById(Long id);
}
