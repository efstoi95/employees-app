package com.enterprise.employees.repository;

import com.enterprise.employees.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Check if a resource exists by name.
     *
     * @param  name  the name of the resource to check
     * @return       true if a resource with the given name exists, false otherwise
     */
    boolean existsByName(String name);

    boolean existsByQrCode(String qrCode);
}
