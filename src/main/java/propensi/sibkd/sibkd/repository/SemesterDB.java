package propensi.sibkd.sibkd.repository;

import propensi.sibkd.sibkd.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterDB extends JpaRepository<Semester, Long> {
    Optional<Semester> findByIdSemester(Long idSemester);
}
