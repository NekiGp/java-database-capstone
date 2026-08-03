# Smart Clinic Management System Architecture

## Architecture Summary

This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases: MySQL for structured data such as patients, doctors, appointments, and administrators, and MongoDB for flexible prescription documents. All requests pass through the controller layer, which delegates business logic to the service layer before accessing the appropriate repositories. JPA entities are used for MySQL, while MongoDB uses document models.

## Numbered Flow of Data and Control

1. Users access the application through Thymeleaf dashboards or REST API clients.
2. Requests are routed to the appropriate MVC Controller or REST Controller.
3. Controllers delegate processing to the Service Layer.
4. The Service Layer applies business rules and communicates with the Repository Layer.
5. Repositories access either the MySQL database or the MongoDB database.
6. Retrieved data is mapped into Java entities or MongoDB document models.
7. The application returns either rendered HTML pages or JSON responses to the client.
