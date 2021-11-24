package com.example.demo.repository;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeePage;
import com.example.demo.model.EmployeeSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class EmployeeCriteriaRepository {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    @Autowired
    public EmployeeCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public Page<Employee> findAllWithFilters(EmployeePage employeePage, EmployeeSearchCriteria employeeSearchCriteria) {

        // For which entity we want to create a query. Result of the criteriaQuery will be the Employee type.
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        // From which entity we want to retrieve the result. It will be the same type.
        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);

        // Predicates will filter the result.
        Predicate predicate = getPredicate(employeeSearchCriteria, employeeRoot);

        // Use predicate. Filter by firstName or lastName.
        criteriaQuery.where(predicate);

        // Sorting.
        setOrder(employeePage, criteriaQuery, employeeRoot);

        // Request to DB.
        TypedQuery<Employee> typedQuery = entityManager.createQuery(criteriaQuery);

        // Set first result that we will return.
        typedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());

        // Define how many entries we want to return.
        typedQuery.setMaxResults(employeePage.getPageSize());

        // Abstract interface for pagination information.
        Pageable pageable = getPageable(employeePage);

        // Count query. Retrieve total amount of entities with applied predicates.
        long employeesCount = getEmployeesCount(predicate);

        return new PageImpl<>(typedQuery.getResultList(), pageable, employeesCount);
    }

    private Predicate getPredicate(EmployeeSearchCriteria employeeSearchCriteria,
                                   Root<Employee> employeeRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if(Objects.nonNull(employeeSearchCriteria.getFirstName())){
            predicates.add(
                    criteriaBuilder.like(employeeRoot.get("firstName"),
                            "%" + employeeSearchCriteria.getFirstName() + "%")
            );
        }
        if(Objects.nonNull(employeeSearchCriteria.getLastName())){
            predicates.add(
                    criteriaBuilder.like(employeeRoot.get("lastName"),
                            "%" + employeeSearchCriteria.getLastName() + "%")
            );
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void setOrder(EmployeePage employeePage,
                          CriteriaQuery<Employee> criteriaQuery,
                          Root<Employee> employeeRoot) {
        if(employeePage.getSortDirection().equals(Sort.Direction.ASC)){
            criteriaQuery.orderBy(criteriaBuilder.asc(employeeRoot.get(employeePage.getSortBy())));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.desc(employeeRoot.get(employeePage.getSortBy())));
        }
    }

    private Pageable getPageable(EmployeePage employeePage) {
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        return PageRequest.of(employeePage.getPageNumber(),employeePage.getPageSize(), sort);
    }

    private long getEmployeesCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Employee> countRoot = countQuery.from(Employee.class);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
