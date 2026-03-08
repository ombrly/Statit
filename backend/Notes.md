# General Notes

- `@Service` annotation tells Spring to make the annotated class a singleton and can inject into anywhere else that calls it, no need for a singleton
- `@Repository` annotation tells Spring Boot to write the annotated interfaces functions for you
- `@Query("<SQL query>")` annotation tells Spring to run the specified SQL code when calling its annotated function
- `@RestController` annotation tells Spring that the annotated class utilizes the network
- `RequestMapping("<mapping>")` annotation tells Spring to reserve the mapping for the annotated class, and nested mappings will have the requested mapping as a suffix
- `@PostMapping("<mapping>")` annotation tells Spring that the annotated function handles post requests from the network at the specified mapping
- `@GetMapping("<mapping>")` annotation tells Spring that the annotated function handles get requests from the network at the specified mapping
- Jackson looks for `is` or `get` prefixes to serialize model fields for `GET` requests