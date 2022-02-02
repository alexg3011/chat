package ru.job4j.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.domain.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {

}
