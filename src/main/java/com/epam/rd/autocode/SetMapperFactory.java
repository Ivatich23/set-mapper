package com.epam.rd.autocode;


import com.epam.rd.autocode.domain.Employee;
import com.epam.rd.autocode.domain.FullName;
import com.epam.rd.autocode.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SetMapperFactory {

    public SetMapper<Set<Employee>> employeesSetMapper() {
        return resultSet -> {
            Set<Employee> employees = new HashSet<>();
            while (resultSet.next()) {
                try {
                    BigInteger id;
                    FullName fullName;
                    LocalDate hired;
                    Position position;
                    BigDecimal salary;
                    Employee manager = null;
                    position = (Position.valueOf(resultSet.getString("POSITION")));
                    id = new BigInteger(resultSet.getString("id"));
                    String firstName = resultSet.getString("FIRSTNAME");
                    String lastName = resultSet.getString("LASTNAME");
                    String middleName = resultSet.getString("MIDDLENAME");
                    fullName = new FullName(firstName, lastName, middleName);
                    Date hiredate = resultSet.getDate("HIREDATE");
                    hired = hiredate.toLocalDate();
                    salary = resultSet.getBigDecimal("SALARY");
                    if (resultSet.getString("MANAGER") != null) {
                        manager = new Employee(new BigInteger(resultSet.getString("MANAGER")), null,
                                null, null, new BigDecimal(0), null);
                    }
                    Employee employee = new Employee(id, fullName, position, hired, salary, manager);
                    employees.add(employee);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            Set<Employee> employeesWithManagers = new HashSet<>();
            for (Employee employee : employees) {
                Employee manager = null;
                if (employee.getManager() != null) {
                    BigInteger managerId = employee.getManager().getId();

                    Optional<Employee> employeeOptional = employees.stream()
                            .filter(e -> e.getId().equals(managerId)).findAny();

                    manager = employeeOptional.orElse(null);
                }
                employeesWithManagers.add(new Employee(employee.getId(), employee.getFullName()
                        , employee.getPosition(), employee.getHired(), employee.getSalary(), manager));
                employeesWithManagers.add(fillEmployeeWithManager(employee, manager, employees));


            }
            return employeesWithManagers;
        };
    }

    private Employee fillEmployeeWithManager(Employee employee, Employee manager, Set<Employee> employees) {
        Employee managersManager = null;
        if (manager != null && manager.getManager() != null) {
            BigInteger managerId = manager.getManager().getId();

            managersManager = employees.stream()
                    .filter(e -> e.getId().equals(managerId)).findAny().orElse(null);
        }

            return new Employee(employee.getId(), employee.getFullName()
                    , employee.getPosition(), employee.getHired(), employee.getSalary(),
                    manager != null ? fillEmployeeWithManager(manager, managersManager, employees) : null);
        }

    }
