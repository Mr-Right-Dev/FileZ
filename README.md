# What it does:
It hosts a web application that let you (and your family/friend) share files/backup for "FREE".
* You can share files with other people.
* Make admins to handle users.
* You can set per user a total amount of storage.


# How to set up:
* Install MySQL.    
  * Create a user called `fileZ` and set your password.
    * `CREATE USER 'fileZ'@'localhost' IDENTIFIED BY 'your_password';`
  * Create a database called `filez`.
    * `CREATE DATABASE filez;`
  * Grant all permissions to the `fileZ` to the database `filez`.
    * `GRANT GRANT ALL PRIVILEGES ON filez.* TO 'fileZ'@'localhost';`
* Create an `.evn.properties` on the root of the project.
  * Use this template:
```properties
# Google Login
spring.security.oauth2.client.registration.google.client-id=<your google client id>
spring.security.oauth2.client.registration.google.client-secret=<your google secret>

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/filez
spring.datasource.username=fileZ
spring.datasource.password=<your mysql password>
```

Made by [MrRight](https://github.com/Mr-Right-Dev/FileZ).