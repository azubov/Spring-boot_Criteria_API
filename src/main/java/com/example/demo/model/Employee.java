package com.example.demo.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee extends AbstractEntity {

    private String firstName;
    private String lastName;

}
