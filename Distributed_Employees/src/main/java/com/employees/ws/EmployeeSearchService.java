package com.employees.ws;

public interface EmployeeSearchService {

	public String searchAllEmployees();

	public String searchEmployeeById(String id);

	public String searchEmployeesByName(String name);

	public String searchEmployeesBySurname(String surname);

	public String searchEmployeeByUsername(String username);

	public String searchEmployeesByEmail(String email);

	public String searchEmployeesByDepartment(String detpartment);

	public String searchEmployeesByPhoneNumber(String phoneNumber);

}
